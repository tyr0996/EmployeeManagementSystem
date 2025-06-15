package hu.martin.ems.vaadin.component.AccessManagement;

import com.google.gson.Gson;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.IEmsFilterableGridPage;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import hu.martin.ems.vaadin.core.EmsDialog;
import hu.martin.ems.vaadin.core.IEmsOptionColumnBaseDialogCreationForm;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@CssImport("./styles/grid.css")
@RolesAllowed("ROLE_PermissionMenuOpenPermission")
@NeedCleanCoding
@Route(value = "/accessManagement/list/permission", layout = MainView.class)
public class PermissionList extends AccessManagement implements Creatable<Permission>, IEmsFilterableGridPage<PermissionList.PermissionVO>, IEmsOptionColumnBaseDialogCreationForm<Permission, PermissionList.PermissionVO> {
    private boolean withDeleted = false;
    @Getter
    private PaginatedGrid<PermissionVO, String> grid;
    @Getter
    private final PermissionApiClient apiClient = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);

    private Grid.Column<PermissionVO> idColumn;
    private Grid.Column<PermissionVO> nameColumn;
    private Grid.Column<PermissionVO> extraData;
    private TextFilteringHeaderCell idFilter;

    private TextFilteringHeaderCell nameFilter;
    List<PermissionVO> permissionVOS;

    private static TextField nameField;
    private static MultiSelectComboBox<Role> roles;

    private Logger logger = LoggerFactory.getLogger(PermissionList.class);

    private EmsDialog createDialog;

    List<Role> roleList;
    List<Permission> permissionList;

    private Gson gson = BeanProvider.getBean(Gson.class);


    @Autowired
    public PermissionList(PaginationSetting paginationSetting) {
        super(paginationSetting);
        this.currentView = this.getClass();

        PermissionVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(PermissionVO.class);


        idColumn = grid.addColumn(v -> v.id);
        nameColumn = grid.addColumn(v -> v.name);

        grid.addClassName("styling");
        grid.setPartNameGenerator(permissionVO -> permissionVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        extraData = this.grid.addComponentColumn(permission -> createOptionColumn("Permission", permission));

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox withDeletedCheckbox = new Checkbox("Show deleted");
        withDeletedCheckbox.addValueChangeListener(event -> {
            withDeleted = event.getValue();
            List<String> newValue = withDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            PermissionVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(withDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, withDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        setFilteringHeaderRow();
        updateGridItems();

        add(hl, grid);
    }

    public void setEntities() {
        EmsResponse response = apiClient.findAllWithDeleted();
        switch (response.getCode()) {
            case 200:
                permissionList = (List<Permission>) response.getResponseData();
                break;
            default:
                permissionList = null;
                logger.error("Permission findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    public void updateGridItems() {
        setEntities();
        if (permissionList != null) {
            permissionVOS = permissionList.stream().map(PermissionVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        } else {
            Notification.show("EmsError happened while getting permissions")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public EmsDialog getSaveOrUpdateDialog(PermissionVO entity) {
        Button saveButton = new Button("Save");
        createDialog = new EmsDialog((entity == null ? "Create" : "Modify") + " permission");
        FormLayout formLayout = new FormLayout();

        nameField = new TextField("Name");

        roles = new MultiSelectComboBox<>("Roles");
        ComboBox.ItemFilter<Role> filterRole = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        setupRoles();
        if (roleList == null) {
            roles.setInvalid(true);
            roles.setEnabled(false);
            roles.setErrorMessage("EmsError happened while getting roles");
            saveButton.setEnabled(false);
        } else {
            roles.setItems(filterRole, roleList);
            roles.setItemLabelGenerator(Role::getName);
        }

        if (entity != null) {
            nameField.setValue(entity.original.getName());
            roles.setValue(entity.original.getRoles());
        }

        saveButton.addClickListener(event -> {
            saveRolesWithPermission(entity == null ? null : entity.original);
            updateGridItems();
        });

        formLayout.add(nameField, roles, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }


    private void setupRoles() {
        EmsResponse response = roleApi.findAll();
        switch (response.getCode()) {
            case 200:
                roleList = (List<Role>) response.getResponseData();
                break;
            default:
                roleList = null;
                logger.error("Role findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private void saveRolesWithPermission(Permission entity) {
        Boolean isUpdate = true;
        if (entity == null) {
            entity = new Permission();
            entity.setDeleted(0L);
            isUpdate = false;
        }
        entity.setName(nameField.getValue());


        EmsResponse response = isUpdate ? apiClient.update(entity) : apiClient.save(entity);
        switch (response.getCode()) {
            case 200:
                Notification.show("Permission " + (isUpdate ? "updated: " : "saved: ") + entity.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                break;
            default:
                Notification.show("Permission " + (isUpdate ? "modifying " : "saving ") + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                createDialog.close();
                updateGridItems();
                break;
        }
        nameField.clear();
        createDialog.close();
    }

    private Stream<PermissionVO> getFilteredStream() {
        return permissionVOS.stream().filter(permissionVO ->
                filterField(idFilter, permissionVO.id.toString()) &&
                filterField(nameFilter, permissionVO.name) &&
                permissionVO.filterExtraData()
        );
    }

    private void setFilteringHeaderRow() {
        idFilter = new TextFilteringHeaderCell("Search id...", this);
        nameFilter = new TextFilteringHeaderCell("Search name...", this);

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if (extraDataFilter.getValue().isEmpty()) {
                PermissionVO.extraDataFilterMap.clear();
            } else {
                PermissionVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        HeaderRow filterRow = grid.appendHeaderRow();

        filterRow.getCell(idColumn).setComponent(styleFilterField(idFilter, "ID"));
        filterRow.getCell(nameColumn).setComponent(styleFilterField(nameFilter, "Name"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }

    public class PermissionVO extends BaseVO<Permission> {
        private String name;

        public PermissionVO(Permission permission) {
            super(permission.id, permission.getDeleted(), permission);
            this.deleted = permission.getDeleted();
            this.id = permission.getId();
            this.name = permission.getName();
        }
    }
}
package hu.martin.ems.vaadin.component.AccessManagement;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.IconProvider;
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
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
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
public class PermissionList extends AccessManagement implements Creatable<Permission>, IEmsFilterableGridPage<PermissionList.PermissionVO> {
    private boolean withDeleted = false;
    @Getter
    private PaginatedGrid<PermissionVO, String> grid;
    private final PaginationSetting paginationSetting;
    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
//    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);

    private Grid.Column<PermissionVO> idColumn;
    private Grid.Column<PermissionVO> nameColumn;
    private Grid.Column<PermissionVO> extraData;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();

    private TextFilteringHeaderCell idFilter;

    private TextFilteringHeaderCell nameFilter;
    List<PermissionVO> permissionVOS;

    private static TextField nameField;
    private static MultiSelectComboBox<Role> roles;

    private Logger logger = LoggerFactory.getLogger(PermissionList.class);

    private Dialog createDialog;

    List<Role> roleList;
    List<Permission> permissionList;

    private Gson gson = BeanProvider.getBean(Gson.class);

//    private MainView mainView;

    @Autowired
    public PermissionList(PaginationSetting paginationSetting) {
        super(paginationSetting);
        this.paginationSetting = paginationSetting;

        PermissionVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(PermissionVO.class);


        idColumn = grid.addColumn(v -> v.id);
        nameColumn = grid.addColumn(v -> v.name);

        //this.grid.setColumns("id", "name");
        grid.addClassName("styling");
        grid.setPartNameGenerator(permissionVO -> permissionVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());


        extraData = this.grid.addComponentColumn(permission -> {
            Button editButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).EDIT_ICON));
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).PERMANENTLY_DELETE_ICON));
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(permission.original);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                permissionApi.restore(permission.original);
                Notification.show("Permission restored: " + permission.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.permissionApi.delete(permission.original);
                switch (resp.getCode()){
                    case 200: {
                        Notification.show("Permission deleted: " + permission.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setupPermissions();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                permissionApi.permanentlyDelete(permission.id);
                Notification.show("Permission permanently deleted: " + permission.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (permission.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });



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

    private void setupPermissions() {
        EmsResponse response = permissionApi.findAllWithDeleted();
        switch (response.getCode()){
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
        setupPermissions();
        if(permissionList != null){
            permissionVOS = permissionList.stream().map(PermissionVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
        else{
            Notification.show("EmsError happened while getting permissions")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void appendCloseButton(Dialog d){
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> d.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        d.getHeader().add(closeButton);
    }

    public Dialog getSaveOrUpdateDialog(Permission entity) {
        Button saveButton = new Button("Save");
        createDialog = new Dialog((entity == null ? "Create" : "Modify") + " permission");
        FormLayout formLayout = new FormLayout();

        nameField = new TextField("Name");
        appendCloseButton(createDialog);

        roles = new MultiSelectComboBox<>("Roles");
        ComboBox.ItemFilter<Role> filterRole = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        setupRoles();
        if(roleList == null){
            roles.setInvalid(true);
            roles.setEnabled(false);
            roles.setErrorMessage("EmsError happened while getting roles");
            saveButton.setEnabled(false);
        }
        else{
            roles.setItems(filterRole, roleList);
            roles.setItemLabelGenerator(Role::getName);
        }

        if (entity != null) {
            nameField.setValue(entity.getName());
            roles.setValue(entity.getRoles());
        }

        saveButton.addClickListener(event -> {
            saveRolesWithPermission(entity);
            updateGridItems();
        });

        formLayout.add(nameField, roles, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }


    private void setupRoles() {
        EmsResponse response = roleApi.findAll();
        switch (response.getCode()){
            case 200:
                roleList = (List<Role>) response.getResponseData();
                break;
            default:
                roleList = null;
                logger.error("Role findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private void saveRolesWithPermission(Permission entity){
        Boolean isUpdate = true;
        if(entity == null){
            entity = new Permission();
            entity.setDeleted(0L);
            isUpdate = false;
        }
        entity.setName(nameField.getValue());


        EmsResponse response = isUpdate ? permissionApi.update(entity) : permissionApi.save(entity);
        switch (response.getCode()){
            case 200:
                Notification.show("Permission " + (isUpdate ? "updated: " : "saved: ") + entity.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                break;
            default:
                Notification.show("Permission " + (isUpdate ? "modifying " : "saving " ) + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                createDialog.close();
                updateGridItems();
                break;
        }
        nameField.clear();
        createDialog.close();
    }

    private Stream<PermissionVO> getFilteredStream() {
        return permissionVOS.stream().filter(permissionVO ->
                (idFilter.isEmpty() || permissionVO.id.toString().toLowerCase().contains(idFilter.getFilterText().toLowerCase())) &&
                        (nameFilter.isEmpty() || permissionVO.name.toLowerCase().contains(nameFilter.getFilterText().toLowerCase())) &&
                        permissionVO.filterExtraData()
        );
    }

    private void setFilteringHeaderRow(){
        idFilter = new TextFilteringHeaderCell("Search id...", this);
        nameFilter = new TextFilteringHeaderCell("Search name...", this);

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                PermissionVO.extraDataFilterMap.clear();
            }
            else{
                PermissionVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        HeaderRow filterRow = grid.appendHeaderRow();;
        filterRow.getCell(idColumn).setComponent(filterField(idFilter, "ID"));
        filterRow.getCell(nameColumn).setComponent(filterField(nameFilter, "Name"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }

    private Component filterField(TextField filterField, String title){
        VerticalLayout res = new VerticalLayout();
        res.getStyle().set("padding", "0px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");
        filterField.getStyle().set("display", "flex").set("width", "100%");
        NativeLabel titleLabel = new NativeLabel(title);
        res.add(titleLabel, filterField);
        res.setClassName("vaadin-header-cell-content");
        return res;
    }


    public class PermissionVO extends BaseVO {
        @NotNull
        private Permission original;
        private String name;

        public PermissionVO(Permission permission){
            super(permission.id, permission.getDeleted());
            this.original = permission;
            this.deleted = permission.getDeleted();
            this.id = permission.getId();
            this.name = permission.getName();
        }
    }
}
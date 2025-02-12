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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/grid.css")
@RolesAllowed("ROLE_PermissionMenuOpenPermission")
@NeedCleanCoding
@Route(value = "/accessManagement/list/permission", layout = MainView.class)
public class PermissionList extends AccessManagement implements Creatable<Permission> {
    private boolean withDeleted = false;
    private PaginatedGrid<PermissionVO, String> grid;
    private final PaginationSetting paginationSetting;
    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
//    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);

    private Grid.Column<PermissionVO> idColumn;
    private Grid.Column<PermissionVO> nameColumn;
    private Grid.Column<PermissionVO> extraData;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();

    private static String idFilterText = "";

    private static String nameFilterText = "";
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
        updateGridItems();

        idColumn = grid.addColumn(v -> v.id);
        nameColumn = grid.addColumn(v -> v.name);

        //this.grid.setColumns("id", "name");
        grid.addClassName("styling");
        grid.setPartNameGenerator(permissionVO -> permissionVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());


        extraData = this.grid.addComponentColumn(permission -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
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

        setFilteringHeaderRow();

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

    private void updateGridItems() {
        setupPermissions();
        if(permissionList != null){
            permissionVOS = permissionList.stream().map(PermissionVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
        else{
            Notification.show("Error happened while getting permissions")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public Dialog getSaveOrUpdateDialog(Permission entity) {
        Button saveButton = new Button("Save");
        createDialog = new Dialog((entity == null ? "Create" : "Modify") + " permission");
        FormLayout formLayout = new FormLayout();

        nameField = new TextField("Name");

        roles = new MultiSelectComboBox<>("Roles");
        ComboBox.ItemFilter<Role> filterRole = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        setupRoles();
        if(roleList == null){
            roles.setInvalid(true);
            roles.setEnabled(false);
            roles.setErrorMessage("Error happened while getting roles");
            saveButton.setEnabled(false);
        }
        else{
            roles.setItems(filterRole, roleList);
            roles.setItemLabelGenerator(Role::getName);
        }

        if (entity != null) {
            nameField.setValue(entity.getName());
            roles.setValue(entity.getRoles());
//            if(entity.getRoles() != null){
//                roles.setValue(entity.getRoles());
//            }
//            else{
//                roles.setErrorMessage("Error happened while getting paired roles");
//                roles.setEnabled(false);
//                roles.setInvalid(true);
//                saveButton.setEnabled(false);
//            }
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
//        EmsResponse responseFindAllOriginalRoles = roleApi.findAllByIds(entity.getRoleIds());
//        List<Role> originalRoles = new ArrayList<>();
//        switch (responseFindAllOriginalRoles.getCode()){
//            case 200:
//                originalRoles = (List<Role>) responseFindAllOriginalRoles.getResponseData();
//                break;
//            case 500:
//                Notification.show(responseFindAllOriginalRoles.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
//                createDialog.close();
//                return;
//            default:
//                Notification.show("Not expected status-code in " + (entity == null ? "saving" : "modifying"));
//                createDialog.close();
//                return;
//        }
//        if(entity != null){
//            entity.setRoles(roles.getSelectedItems());
//            entity.setName(nameField.getValue());
//        }
        Boolean isUpdate = true;
        if(entity == null){
            entity = new Permission();
            entity.setDeleted(0L);
            isUpdate = false;
        }
//        entity.setRoleIds(roles.getSelectedItems().stream().map(Role::getId).toList());
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
//        Permission originalPermission = null;
//        List<Role> originalRoles = null;
//        if(entity != null){
//            originalPermission = new Permission();
//            originalPermission.setName(entity.getName());
//            originalPermission.setDeleted(entity.getDeleted());
//            originalPermission.id = entity.id;
//            List<Role> pairedRoles = getAllPairedRoleTo(entity);
//            if(pairedRoles == null){
//                Notification.show("Error happened while getting paired permissions")
//                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
//                nameField.clear();
//                createDialog.close();
//                return;
//            }
//            else{
//                originalRoles = pairedRoles;
//            }
//
//        }
//
//        Permission permission = null;
//        if(entity != null){
//            permission = entity;
//        }
//        else{
//            permission = new Permission();
//        }
//
//        permission.setDeleted(0L);
//        permission.setName(nameField.getValue());
//        EmsResponse permissionResponse = null;
//        if(entity != null){
//            permissionResponse = permissionApi.update(permission);
//        }
//        else{
//            permissionResponse = permissionApi.save(permission);
//        }
//        switch (permissionResponse.getCode()){
//            case 200:
//                permission = (Permission) permissionResponse.getResponseData();
//                break;
//            case 500:
//                Notification.show(permissionResponse.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
//                createDialog.close();
//                return;
//            default:
//                Notification.show("Not expected status-code in " + (entity == null ? "saving" : "modifying"));
//                createDialog.close();
//                return;
//        }
//        roleXPermissionApi.removeAllRolesFrom(entity);
//        List<Role> selectedRoles = roles.getSelectedItems().stream().toList();
//        for(int i = 0; i < selectedRoles.size(); i++){
//            RoleXPermission rxp = new RoleXPermission(selectedRoles.get(i), permission);
//            rxp.setDeleted(0L);
//            EmsResponse roleXPermissionResponse = roleXPermissionApi.save(rxp);
//            switch (roleXPermissionResponse.getCode()){
//                case 200: break;
//                case 500:
//                    if(entity == null){
//                        undoSave((Permission) permissionResponse.getResponseData());
//                    }
//                    else{
//                        undoUpdate(originalPermission, originalRoles);
//                    }
//                    Notification.show(roleXPermissionResponse.getDescription());
//                    createDialog.close();
//                    return;
//                default:
//                    if(entity == null){
//                        undoSave((Permission) permissionResponse.getResponseData());
//                    }
//                    else{
//                        undoUpdate(originalPermission, originalRoles);
//                    }
//                    Notification.show("Not expected status-code in " + (entity == null ? "saving" : "modifying"));
//                    createDialog.close();
//                    return;
//            }
//        }




        nameField.clear();
        createDialog.close();
    }

    private Stream<PermissionVO> getFilteredStream() {
        return permissionVOS.stream().filter(permissionVO ->
                (idFilterText.isEmpty() || permissionVO.id.toString().toLowerCase().contains(idFilterText.toLowerCase())) &&
                        (nameFilterText.isEmpty() || permissionVO.name.toLowerCase().contains(nameFilterText.toLowerCase())) &&
                        permissionVO.filterExtraData()
        );
    }

    private void setFilteringHeaderRow(){
        TextField idColumnFilter = new TextField();
        idColumnFilter.setPlaceholder("Search id...");
        idColumnFilter.setClearButtonVisible(true);
        idColumnFilter.addValueChangeListener(event -> {
            idFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField nameColumnFilter = new TextField();
        nameColumnFilter.setPlaceholder("Search name...");
        nameColumnFilter.setClearButtonVisible(true);
        nameColumnFilter.addValueChangeListener(event -> {
            nameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

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

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();;
        filterRow.getCell(idColumn).setComponent(filterField(idColumnFilter, "ID"));
        filterRow.getCell(nameColumn).setComponent(filterField(nameColumnFilter, "Name"));
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
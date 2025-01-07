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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/grid.css")
@AnonymousAllowed
@NeedCleanCoding
public class RoleList extends VerticalLayout implements Creatable<Role> {
    private boolean showDeleted = false;
    private PaginatedGrid<RoleVO, String> grid;
    private final PaginationSetting paginationSetting;
    private HorizontalLayout buttonsLayout;
    private Button saveButton;
    private TextField nameField;
    private MultiSelectComboBox<Permission> permissions;
    private Role editableRole;

    private Dialog createOrModifyDialog;
    private FormLayout createOrModifyForm;
    private List<Role> roles;
    private List<RoleVO> roleVOS;

    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
//    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);

    private static String roleFilterText = "";
    private static Set<Permission> permissionsFilterSet = new HashSet<>();
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<RoleVO> extraData;
    private Grid.Column<RoleVO> roleColumn;
    private Grid.Column<RoleVO> permissionsColumn;

    private Logger logger = LoggerFactory.getLogger(RoleList.class);
    List<Permission> permissionList;
    private Gson gson = BeanProvider.getBean(Gson.class);

    public RoleList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        RoleVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.roles = new ArrayList<>();
        this.createOrModifyForm = new FormLayout();
        createRoleXPermissionGrid();
        createLayout();
    }

    private void createRoleXPermissionGrid(){
        setRoles();
//        if(roles != null){
//            List<Role> failed = roles.stream().filter(v -> v.getPermissions() == null).collect(Collectors.toList());
//            if(failed.size() != 0){
//                roles = null;
//                System.out.println("most");
//            }
//        }

        this.grid = new PaginatedGrid<>(RoleVO.class);
        grid.addClassName("styling");
        grid.setPartNameGenerator(roleVO -> roleVO.original.getDeleted() != 0 ? "deleted" : null);
        setGridColumns();
        setFilteringHeaderRow();

        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());
        updateGridItems();
    }

    private void createLayout(){
        Button create = new Button("Create");
        create.addClickListener(event -> {
            generateSaveOrUpdateDialog();
            createOrModifyDialog.open();
        });

        Checkbox withDeletedCheckbox = new Checkbox("Show deleted");
        withDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            RoleVO.showDeletedCheckboxFilter.replace("deleted", newValue);
            setRoles();
            updateGridItems();
        });

        buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(create, withDeletedCheckbox);
        buttonsLayout.setAlignSelf(Alignment.CENTER, create);

        add(buttonsLayout, grid);
    }

    public void generateSaveOrUpdateDialog() {
        createOrModifyDialog = new Dialog((editableRole == null ? "Create" : "Modify") + " role");
        createSaveOrUpdateForm();
        saveButton.addClickListener(event -> {
            saveRoleWithPermissions();
            setRoles();
            nameField.clear();
            createOrModifyDialog.close();
            updateGridItems();
        });

        createOrModifyDialog.add(this.createOrModifyForm);
    }

    private void createSaveOrUpdateForm(){
        this.createOrModifyForm.removeAll();
        nameField = new TextField("Name");
        saveButton = new Button("Save");
        //Button editRoleXPermissionButton = new Button("Edit permissions");

        permissions = new MultiSelectComboBox<>("Permission");
        ComboBox.ItemFilter<Permission> filterPermission = (permission, filterString) ->
                permission.getName().toLowerCase().contains(filterString.toLowerCase());
        setupPermissions();
        if(permissionList == null){
            permissions.setInvalid(true);
            permissions.setErrorMessage("Error happened while getting permissions");
            permissions.setEnabled(false);
            saveButton.setEnabled(false);
        }
        else{
            saveButton.setEnabled(true);
            permissions.setInvalid(false);
            permissions.setEnabled(true);
            permissions.setItems(filterPermission, permissionList);
            permissions.setItemLabelGenerator(Permission::getName);
            if (editableRole != null) {
                nameField.setValue(editableRole.getName());
                List<Permission> editableRolePermissions = editableRole.getPermissions().stream().toList();
                permissions.setValue(editableRolePermissions);

            }
        }
        createOrModifyForm.add(nameField, permissions, saveButton);
    }

    private void setupPermissions() {
        EmsResponse response = permissionApi.findAll();
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

    private void saveRoleWithPermissions() {
        EmsResponse resp = null;
        if (editableRole == null) {
            Role r = new Role();
            r.setPermissions(permissions.getSelectedItems());
            r.setName(nameField.getValue());
            r.setDeleted(0L);
            resp = roleApi.save(r);
        }
        else{
            editableRole.setPermissions(permissions.getSelectedItems());
            editableRole.setName(nameField.getValue());
            resp = roleApi.update(editableRole);
        }

        updateGridItems();
        switch (resp.getCode()){
            case 200: {
                Notification.show("Role " + (editableRole == null ? "saved: " : "updated: ") + nameField.getValue())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                break;
            }
            case 500: {
                Notification.show("Role " + (editableRole == null ? "saving " : "modifying ") + "failed")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                break;
            }
            default: {
                Notification.show("Not expected status-code in " + (editableRole == null ? "saving" : "modifying")).addThemeVariants(NotificationVariant.LUMO_WARNING);
                logger.warn("Invalid status code in AddressList: {}", resp.getCode());
                break;
            }
        }



//        Role originalRole = null;
//        List<Permission> originalPermissions = null;
//
//        if(editableRole != null){
//            originalRole = editableRole;
////            originalPermissions = roleXPermissionApi.findAllPairedPermissionsTo(editableRole);
//            originalPermissions = getAllPairedPermissionsTo(editableRole);
//            if(originalPermissions == null){
//                Notification.show("Error happened while getting permissions to role")
//                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
//                return;
//            }
//        }
//
//        List<Role> savedRoles = new ArrayList<>();
//        List<Permission> savedPermissions = new ArrayList<>();
//        List<RoleXPermission> savedRoleXPermissions = new ArrayList<>();
//
//        Role role = null;
//        if(editableRole == null){
//            role = new Role();
//        }
//        else{
//            role = new Role();
//            role.setDeleted(editableRole.getDeleted());
//            role.setName(editableRole.getName());
//            role.id = editableRole.getId();
//        }
//        role.setName(nameField.getValue());
//        role.setDeleted(0L);
//        EmsResponse response = null;
//
//        if(editableRole != null){
//            response = roleApi.update(role);
//        }
//        else{
//            response = roleApi.save(role);
//        }
//        switch (response.getCode()){
//            case 200:
//                role = (Role) response.getResponseData();
//                break;
//            case 500:
//                Notification.show(response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
//                createOrModifyDialog.close();
//                updateGridItems();
//                return;
//            default:
//                Notification.show("Not expected status-code in " + (editableRole == null ? "saving" : "modifying")).addThemeVariants(NotificationVariant.LUMO_WARNING);
//                logger.warn("Invalid status code in RoleList: {}", response.getCode());
//                createOrModifyDialog.close();
//
//                updateGridItems();
//                return;
//        }
//        roleXPermissionApi.removeAllPermissionsFrom(role);
//        for(Permission p : permissions.getSelectedItems()){
//            RoleXPermission rxp = new RoleXPermission(role, p);
//            rxp.setDeleted(0L);
//            EmsResponse responseRoleXPermission = roleXPermissionApi.save(rxp);
//            switch (responseRoleXPermission.getCode()){
//                case 200:
//                    savedRoleXPermissions.add((RoleXPermission) responseRoleXPermission.getResponseData());
//                    break;
//                case 500:
//                    Notification.show(responseRoleXPermission.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
//                    createOrModifyDialog.close();
//                    if(editableRole == null){
//                        undoSave(role);
//                    }
//                    else{
//                        undoUpdate(originalRole, originalPermissions);
//                    }
//
//                    updateGridItems();
//                    return;
//                default:
//                    Notification.show("Not expected status-code in " + (editableRole == null ? "saving permission to role" : "modifying permission to role")).addThemeVariants(NotificationVariant.LUMO_WARNING);
//                    logger.warn("Invalid status code in RoleList, RoleXPermission saving: {}", responseRoleXPermission.getCode());
//                    createOrModifyDialog.close();
//                    if(editableRole == null){
//                        undoSave(role);
//                    }
//                    else{
//                        undoUpdate(originalRole, originalPermissions);
//                    }
//
//                    updateGridItems();
//                    return;
//            }
//        }
//
//
//        GroupedRoleXPermission grxp =  new GroupedRoleXPermission(role, permissions.getSelectedItems().stream().toList());
//
//
//        updateGridItems();
//        Notification.show("Role " + (editableRole == null ? "saved: " : "updated: ") + grxp.getName())
//                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

    }

//    private List<RoleXPermission> getAlRoleXPermissionByRole(Role r) {
//
//        EmsResponse response = roleXPermissionApi.findAlRoleXPermissionByRole(r);
//        switch (response.getCode()){
//            case 200:
//                return (List<RoleXPermission>) response.getResponseData();
//            default:
//                logger.error("RoleXPermission findAlRoleXPermissionByRoleError. Code: {}, Description: {}", response.getCode(), response.getDescription());
//                return null;
//        }
//    }

    private void setFilteringHeaderRow(){
        TextField roleFilter = new TextField();
        roleFilter.setPlaceholder("Search role...");
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(event -> {
            roleFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        MultiSelectComboBox<Permission> permissionsFilter = new MultiSelectComboBox();
        ComboBox.ItemFilter<Permission> filterPermission = (permission, filterString) ->
                permission.getName().toLowerCase().contains(filterString.toLowerCase());
        setupPermissions();
        if(permissionList == null){
            permissionsFilter.setInvalid(true);
            permissionsFilter.setErrorMessage("Error happened while getting permissions");
            permissionsFilter.setEnabled(false);
        }
        else{
            permissionsFilter.setInvalid(false);
            permissionsFilter.setEnabled(true);
            permissionsFilter.setItems(filterPermission, permissionList);
            permissionsFilter.setItemLabelGenerator(Permission::getName);
        }

        permissionsFilter.addSelectionListener(v -> {
            permissionsFilterSet = v.getAllSelectedItems();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                RoleVO.extraDataFilterMap.clear();
            }
            else{
                RoleVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(roleColumn).setComponent(filterField(roleFilter, "Role"));
        filterRow.getCell(permissionsColumn).setComponent(filterField(permissionsFilter, "Permissions"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }

    private void setGridColumns(){
        roleColumn = this.grid.addColumn(v -> v.role);
        permissionsColumn = this.grid.addColumn(v -> v.permissions);
        addOptionsColumn();
    }

    private Component filterField(Component filterField, String title){
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

    private void addOptionsColumn(){
        extraData = this.grid.addComponentColumn(roleVO -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                editableRole = roleVO.original;
                generateSaveOrUpdateDialog();
                createOrModifyDialog.open();
//                setRoles();
//                updateGridItems();
            });

            restoreButton.addClickListener(event -> {
                roleApi.restore(roleVO.original);
                setRoles();
                updateGridItems();
//                List<RoleXPermission> rxps = getAlRoleXPermissionByRole(roleVO.original.role);
//                if(rxps == null){
//                    Notification.show("Role restore failed")
//                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
//                    roleVO.original.role.setDeleted(0L);
//                    roleApi.delete(roleVO.original.role);
//                    setRoles();
//                    updateGridItems();
//                }
//                else{
//                    for(RoleXPermission rxp : rxps){
//                        roleXPermissionApi.restore(rxp);
//                    }
                    Notification.show("Role restored: " + roleVO.role)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//                    setRoles();
//                    updateGridItems();
//                }
            });

            deleteButton.addClickListener(event -> {
                roleApi.delete(roleVO.original);
                setRoles();
                updateGridItems();
//                List<RoleXPermission> rxps = getAlRoleXPermissionByRole(roleVO.original.role);
//                if(rxps == null){
//                    Notification.show("Role-permission pair deleting failed")
//                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
//                    roleVO.original.role.setDeleted(1L);
//                    roleApi.restore(roleVO.original.role);
//                    setRoles();
//                    updateGridItems();
//                }
//                else{
//                    for(RoleXPermission rxp : rxps){
//                        roleXPermissionApi.delete(rxp);
//                    }
                    Notification.show("Role deleted: " + roleVO.role)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//                    setRoles();
//                    updateGridItems();
//                }
            });

            permanentDeleteButton.addClickListener(event -> {
                roleApi.permanentlyDelete(roleVO.id);
//                List<RoleXPermission> rxps = getAlRoleXPermissionByRole(roleVO.original.role);
//                for(RoleXPermission rxp : rxps){
//                    roleXPermissionApi.permanentlyDelete(rxp.id);
//                }
                Notification.show("Role permanently deleted: " + roleVO.role)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setRoles();
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (roleVO.deleted == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });
    }

    private void updateGridItems() {
        if(roles != null){
            roleVOS = roles.stream().map(RoleVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
        else{
        }
    }

    private Stream<RoleVO> getFilteredStream() {
        return roleVOS.stream().filter(groupedRoleXPermissionVO ->
                (roleFilterText.isEmpty() || groupedRoleXPermissionVO.role.toLowerCase().contains(roleFilterText.toLowerCase())) &&
                        (permissionsFilterSet.isEmpty() || groupedRoleXPermissionVO.permissionSet.containsAll(permissionsFilterSet)) &&
                        groupedRoleXPermissionVO.filterExtraData()
        );
    }

    private void setRoles(){
        List<Role> roles;
        if(showDeleted) {
            roles = (List<Role>) roleApi.findAllWithGraphWithDeleted().getResponseData();
        }
        else{
            roles = (List<Role>) roleApi.findAllWithGraph().getResponseData();
        }
        this.roles.clear();
        this.roles.addAll(roles);
//        List<RoleXPermission> rxps = getAllWithUnused();
//        if(rxps != null){
//            Map<Role, List<Permission>> gridData = new HashMap<>();
//            for (RoleXPermission rxp : rxps) {
//                Role role = rxp.getRole();
//                addRoleIfNotExists(gridData, role);
//            }
//            this.roles.clear();
//            gridData.forEach((role, permissionList) -> {
//                this.roles.add(new GroupedRoleXPermission(role, permissionList));
//            });
//        }
//        else{
//            this.roles = null;
//        }
    }

    protected class RoleVO extends BaseVO {
        private Role original;
        private String role;
        private String permissions;

        private Set<Permission> permissionSet;

        public RoleVO(Role original){
            super(original.getId(), original.getDeleted());
            this.original = original;
//            this.id = original.getId();
//            this.deleted = original.getDeleted();
            this.role = original.getName();
            this.permissions = original.getPermissions().stream()
                    .filter(Objects::nonNull)
                    .map(Permission::getName)
                    .collect(Collectors.joining(", "));
            this.permissionSet = original.getPermissions();
        }
    }
//
//
//    @AllArgsConstructor
//    protected class GroupedRoleXPermission {
//
//        @NotNull
//        private Role role;
//
//        @NotNull
//        private List<Permission> permissions;
//
//        private String getName(){
//            return this.role.getName();
//        }
//
//        private Long getDeleted(){
//            return this.role.getDeleted();
//        }
//
//        private Long getId(){
//            return this.role.getId();
//        }
//
//        public String permissionsAsString() {
//            return null;
//        }
//
//        public String roleAsString() {
//            return role.getName();
//        }
//    }
}
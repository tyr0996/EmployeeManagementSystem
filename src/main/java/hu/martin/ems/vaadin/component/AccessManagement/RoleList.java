package hu.martin.ems.vaadin.component.AccessManagement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.RoleXPermissionApiClient;
import hu.martin.ems.vaadin.component.Creatable;
import lombok.AllArgsConstructor;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/grid.css")
@AnonymousAllowed
@NeedCleanCoding
public class RoleList extends VerticalLayout implements Creatable<Role> {
    private boolean showDeleted = false;
    private PaginatedGrid<GroupedRoleXPermission, String> grid;
    private final PaginationSetting paginationSetting;
    private HorizontalLayout buttonsLayout;
    private Button saveButton;
    private TextField nameField;
    private MultiSelectComboBox<Permission> permissions;
    private Role editableRole;

    private Dialog createOrModifyDialog;
    private FormLayout createOrModifyForm;
    private List<GroupedRoleXPermission> groupedRoleXPermissions;

    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);

    public RoleList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;
        this.groupedRoleXPermissions = new ArrayList<>();
        this.createOrModifyForm = new FormLayout();
        createRoleXPermissionGrid();
        createLayout();
    }

    private void createRoleXPermissionGrid(){
        this.grid = new PaginatedGrid<>(GroupedRoleXPermission.class);
        grid.addClassName("styling");
        grid.setPartNameGenerator(roleXPermission -> roleXPermission.role.getDeleted() != 0 ? "deleted" : null);

        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());
        setGridColumns();
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
            updateGridItems();
        });

        buttonsLayout = new HorizontalLayout();
        buttonsLayout.add(create, withDeletedCheckbox);
        buttonsLayout.setAlignSelf(Alignment.CENTER, create);

        add(buttonsLayout, grid);
    }

    public void generateSaveOrUpdateDialog() {
        RoleXPermissionCreate rxpc = new RoleXPermissionCreate();
        createOrModifyDialog = new Dialog((editableRole == null ? "Create" : "Modify") + " role");
        createSaveOrUpdateForm();
        saveButton.addClickListener(event -> {
            GroupedRoleXPermission grxp = saveRoleWithPermissions();
            Notification.show((editableRole == null ? "Role saved: " : "Role updated: ") + grxp.getName())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
        List<Permission> savedPermissions = permissionApi.findAll();
        permissions.setItems(filterPermission, savedPermissions);
        permissions.setItemLabelGenerator(Permission::getName);

        if (editableRole != null) {
            nameField.setValue(editableRole.getName());
            List<Permission> editableRolePermissions = roleXPermissionApi.findAllPairedPermissionsTo(editableRole);
            permissions.setValue(editableRolePermissions);
        }
        createOrModifyForm.add(nameField, permissions, saveButton);
    }

    private GroupedRoleXPermission saveRoleWithPermissions(){
        Role role = Objects.requireNonNullElseGet(editableRole, Role::new);
        role.setName(nameField.getValue());
        role.setDeleted(0L);
        if(editableRole != null){
            role = roleApi.update(role);
        }
        else{
            role = roleApi.save(role);
        }
        roleXPermissionApi.removeAllPermissionsFrom(role);
        for(Permission p : permissions.getSelectedItems()){
            RoleXPermission rxp = new RoleXPermission(role, p);
            rxp.setDeleted(0L);
            roleXPermissionApi.save(rxp);
        }
        return new GroupedRoleXPermission(role, permissions.getSelectedItems().stream().toList());
    }



    private void setGridColumns(){
        this.grid.addColumn(GroupedRoleXPermission::roleAsString).setHeader("Role");
        this.grid.addColumn(GroupedRoleXPermission::permissionsAsString).setHeader("Permissions");
        addOptionsColumn();
    }

    private void addOptionsColumn(){
        this.grid.addComponentColumn(groupedRoleXPermission -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                createSaveOrUpdateForm();
            });

            restoreButton.addClickListener(event -> {
                roleApi.restore(groupedRoleXPermission.role);
                List<RoleXPermission> rxps = roleXPermissionApi.findAlRoleXPermissionByRole(groupedRoleXPermission.role);
                for(RoleXPermission rxp : rxps){
                    roleXPermissionApi.restore(rxp);
                }
                Notification.show("Role restored: " + groupedRoleXPermission.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                roleApi.delete(groupedRoleXPermission.role);
                List<RoleXPermission> rxps = roleXPermissionApi.findAlRoleXPermissionByRole(groupedRoleXPermission.role);
                for(RoleXPermission rxp : rxps){
                    roleXPermissionApi.delete(rxp);
                }
                Notification.show("Role deleted: " + groupedRoleXPermission.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                roleApi.permanentlyDelete(groupedRoleXPermission.role.id);
                List<RoleXPermission> rxps = roleXPermissionApi.findAlRoleXPermissionByRole(groupedRoleXPermission.role);
                for(RoleXPermission rxp : rxps){
                    roleXPermissionApi.permanentlyDelete(rxp.id);
                }
                Notification.show("Role permanently deleted: " + groupedRoleXPermission.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (groupedRoleXPermission.getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (groupedRoleXPermission.getDeleted() == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setHeader("Options");
    }

    public void refreshGrid(){
        updateGridItems();
    }

    private void updateGridItems() {
        groupedRoleXPermissions.clear();
        setGroupedRoleXPermissions();
        this.grid.setItems(groupedRoleXPermissions);
    }

    private void setGroupedRoleXPermissions(){
        List<RoleXPermission> rxps = roleXPermissionApi.findAllWithUnused(showDeleted);
        Map<Role, List<Permission>> gridData = new HashMap<>();
        for (RoleXPermission rxp : rxps) {
            Role role = rxp.getRole();
            addRoleIfNotExists(gridData, role);
        }
        gridData.forEach((role, permissionList) -> {
            this.groupedRoleXPermissions.add(new GroupedRoleXPermission(role, permissionList));
        });
    }

    private void addRoleIfNotExists(Map<Role, List<Permission>> gridData, Role role) {
        if (!gridData.containsKey(role)) {
            List<Permission> permissions = roleXPermissionApi.findAllPairedPermissionsTo(role);
            gridData.put(role, permissions);
        }
    }


    @AllArgsConstructor
    protected class GroupedRoleXPermission {
        private Role role;
        private List<Permission> permissions;

        private String getName(){
            return this.role.getName();
        }

        private Long getDeleted(){
            return this.role.getDeleted();
        }

        private Long getId(){
            return this.role.getId();
        }

        public String permissionsAsString() {
            return permissions.stream()
                    .filter(Objects::nonNull)
                    .map(Permission::getName)
                    .collect(Collectors.joining(", "));
        }

        public String roleAsString() {
            return role.getName();
        }
    }
}
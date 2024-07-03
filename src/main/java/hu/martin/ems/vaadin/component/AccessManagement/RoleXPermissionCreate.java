package hu.martin.ems.vaadin.component.AccessManagement;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.service.PermissionService;
import hu.martin.ems.service.RoleService;
import hu.martin.ems.service.RoleXPermissionService;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class RoleXPermissionCreate extends VerticalLayout {
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final RoleXPermissionService roleXPermissionService;

    public RoleXPermissionCreate(RoleService roleService,
                                 PermissionService permissionService,
                                 RoleXPermissionService roleXPermissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.roleXPermissionService = roleXPermissionService;

        add(getFormLayout(null, null));
    }

    public FormLayout getFormLayout(@Nullable Role entity, @Nullable Dialog d){
        FormLayout formLayout = new FormLayout();

        ComboBox<Role> roles = new ComboBox<>("Role");
        ComboBox.ItemFilter<Role> filterRole = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        roles.setItems(filterRole, roleService.findAll(false));
        roles.setItemLabelGenerator(Role::getName);

        MultiSelectComboBox<Permission> permissions = new MultiSelectComboBox<>("Permission");
        ComboBox.ItemFilter<Permission> filterPermission = (permission, filterString) ->
                permission.getName().toLowerCase().contains(filterString.toLowerCase());
        permissions.setItems(filterPermission, permissionService.findAll(false));
        permissions.setItemLabelGenerator(Permission::getName);

        roles.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<Role>, Role>>) event -> {
            Role selectedRole = event.getValue();
            if (selectedRole != null) {
                permissions.setValue(roleXPermissionService.findAllPermission(selectedRole));
            }
        });

        if (entity != null) {
            roles.setValue(entity);
            permissions.setValue(roleXPermissionService.findAllPermission(entity));
        }
        if(d != null){
            roles.setEnabled(false);
        }

        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> {
            Role r = roles.getValue();
            roleXPermissionService.clearPermissions(r);
            List<Permission> permission = permissions.getValue().stream().collect(Collectors.toList());
            permission.forEach(p -> {
                RoleXPermission rxp = new RoleXPermission();
                rxp.setRole(r);
                rxp.setPermission(p);
                rxp.setDeleted(0L);
                roleXPermissionService.saveOrUpdate(rxp);
            });
            roles.clear();
            permissions.clear();
            if(d != null){
                d.close();
            }
            Notification.show("Role successfully pairing!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });

        formLayout.add(roles, permissions, saveButton);
        return formLayout;
    }
}

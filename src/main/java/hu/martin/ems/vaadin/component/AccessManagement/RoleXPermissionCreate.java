package hu.martin.ems.vaadin.component.AccessManagement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.RoleXPermissionApiClient;
import jakarta.annotation.Nullable;

import java.util.List;

@AnonymousAllowed
@NeedCleanCoding
public class RoleXPermissionCreate extends VerticalLayout {

    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);

    public RoleXPermissionCreate() {
        add(buildFormLayout(null, null));
    }

    public FormLayout buildFormLayout(@Nullable Role role, @Nullable Dialog dialog) {
        FormLayout formLayout = new FormLayout();

        ComboBox<Role> roleComboBox = createRoleComboBox(role);
        MultiSelectComboBox<Permission> permissionComboBox = createPermissionComboBox(roleComboBox);

        if (role != null) {
            initializeFormWithExistingData(roleComboBox, permissionComboBox, role, dialog);
        }

        Button saveButton = createSaveButton(roleComboBox, permissionComboBox, dialog);

        formLayout.add(roleComboBox, permissionComboBox, saveButton);
        return formLayout;
    }

    private ComboBox<Role> createRoleComboBox(@Nullable Role selectedRole) {
        ComboBox<Role> roleComboBox = new ComboBox<>("Role");
        roleComboBox.setItems((role, filter) -> role.getName().toLowerCase().contains(filter.toLowerCase()), roleApi.findAll());
        roleComboBox.setItemLabelGenerator(Role::getName);

        if (selectedRole != null) {
            roleComboBox.setValue(selectedRole);
        }

        return roleComboBox;
    }

    private MultiSelectComboBox<Permission> createPermissionComboBox(ComboBox<Role> roleComboBox) {
        MultiSelectComboBox<Permission> permissionComboBox = new MultiSelectComboBox<>("Permission");
        permissionComboBox.setItems((permission, filter) -> permission.getName().toLowerCase().contains(filter.toLowerCase()), permissionApi.findAll());
        permissionComboBox.setItemLabelGenerator(Permission::getName);

        roleComboBox.addValueChangeListener(event -> {
            Role selectedRole = event.getValue();
            if (selectedRole != null) {
                permissionComboBox.setValue(roleXPermissionApi.findAllPairedPermissionsTo(selectedRole));
            }
        });

        return permissionComboBox;
    }

    private void initializeFormWithExistingData(ComboBox<Role> roleComboBox, MultiSelectComboBox<Permission> permissionComboBox, Role entity, @Nullable Dialog dialog) {
        roleComboBox.setValue(entity);
        permissionComboBox.setValue(roleXPermissionApi.findAllPairedPermissionsTo(entity));

        if (dialog != null) {
            roleComboBox.setEnabled(false);
        }
    }

    private Button createSaveButton(ComboBox<Role> roleComboBox, MultiSelectComboBox<Permission> permissionComboBox, @Nullable Dialog dialog) {
        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> saveRolePermissions(roleComboBox, permissionComboBox, dialog));

        return saveButton;
    }

    private void saveRolePermissions(ComboBox<Role> roleComboBox, MultiSelectComboBox<Permission> permissionComboBox, @Nullable Dialog dialog) {
        Role selectedRole = roleComboBox.getValue();
        List<Permission> selectedPermissions = permissionComboBox.getValue().stream().toList();

        roleXPermissionApi.removeAllPermissionsFrom(selectedRole);

        selectedPermissions.forEach(permission -> {
            RoleXPermission roleXPermission = new RoleXPermission(selectedRole, permission);
            roleXPermission.setDeleted(0L);
            roleXPermissionApi.save(roleXPermission);
        });

        clearForm(roleComboBox, permissionComboBox, dialog);

        Notification.show("Role successfully paired!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void clearForm(ComboBox<Role> roleComboBox, MultiSelectComboBox<Permission> permissionComboBox, @Nullable Dialog dialog) {
        roleComboBox.clear();
        permissionComboBox.clear();

        if (dialog != null) {
            dialog.close();
        }
    }
}

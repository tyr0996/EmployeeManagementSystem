package hu.martin.ems.vaadin.component.AccessManagement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.RoleXPermissionApiClient;

import java.util.List;

@AnonymousAllowed
@NeedCleanCoding
public class RoleXPermissionCreate extends VerticalLayout {

    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);

    public RoleXPermissionCreate() {
        add(buildFormLayout());
    }

    public FormLayout buildFormLayout() {
        FormLayout formLayout = new FormLayout();

        ComboBox<Role> roleComboBox = createRoleComboBox();
        MultiSelectComboBox<Permission> permissionComboBox = createPermissionComboBox(roleComboBox);

        Button saveButton = createSaveButton(roleComboBox, permissionComboBox);

        formLayout.add(roleComboBox, permissionComboBox, saveButton);
        return formLayout;
    }

    private ComboBox<Role> createRoleComboBox() {
        ComboBox<Role> roleComboBox = new ComboBox<>("Role");
        roleComboBox.setItems((role, filter) -> role.getName().toLowerCase().contains(filter.toLowerCase()), roleApi.findAll());
        roleComboBox.setItemLabelGenerator(Role::getName);
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

    private Button createSaveButton(ComboBox<Role> roleComboBox, MultiSelectComboBox<Permission> permissionComboBox) {
        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> saveRolePermissions(roleComboBox, permissionComboBox));

        return saveButton;
    }

    private void saveRolePermissions(ComboBox<Role> roleComboBox, MultiSelectComboBox<Permission> permissionComboBox) {
        Role selectedRole = roleComboBox.getValue();
        List<Permission> selectedPermissions = permissionComboBox.getValue().stream().toList();

        roleXPermissionApi.removeAllPermissionsFrom(selectedRole);

        selectedPermissions.forEach(permission -> {
            RoleXPermission roleXPermission = new RoleXPermission(selectedRole, permission);
            roleXPermission.setDeleted(0L);
            roleXPermissionApi.save(roleXPermission);
        });

        clearForm(roleComboBox, permissionComboBox);

        Notification.show("Role successfully paired!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void clearForm(ComboBox<Role> roleComboBox, MultiSelectComboBox<Permission> permissionComboBox) {
        roleComboBox.clear();
        permissionComboBox.clear();
    }
}

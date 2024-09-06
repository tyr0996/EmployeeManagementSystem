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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.RoleXPermissionApiClient;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@AnonymousAllowed
public class RoleXPermissionCreate extends VerticalLayout {

    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);

    public  RoleXPermissionCreate() {
        add(getFormLayout(null, null));
    }

    public FormLayout getFormLayout(@Nullable Role entity, @Nullable Dialog d){
        FormLayout formLayout = new FormLayout();

        ComboBox<Role> roles = new ComboBox<>("Role");
        ComboBox.ItemFilter<Role> filterRole = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        roles.setItems(filterRole, roleApi.findAll());
        roles.setItemLabelGenerator(Role::getName);

        MultiSelectComboBox<Permission> permissions = new MultiSelectComboBox<>("Permission");
        ComboBox.ItemFilter<Permission> filterPermission = (permission, filterString) ->
                permission.getName().toLowerCase().contains(filterString.toLowerCase());
        permissions.setItems(filterPermission, permissionApi.findAll());
        permissions.setItemLabelGenerator(Permission::getName);

        roles.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<Role>, Role>>) event -> {
            Role selectedRole = event.getValue();
            if (selectedRole != null) {
                permissions.setValue(roleXPermissionApi.findAllPairedPermissionsTo(selectedRole));
            }
        });

        if (entity != null) {
            roles.setValue(entity);
            permissions.setValue(roleXPermissionApi.findAllPairedPermissionsTo(entity));
        }
        if(d != null){
            roles.setEnabled(false);
        }

        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> {
            Role r = roles.getValue();
            roleXPermissionApi.removeAllPermissionsFrom(r);
            List<Permission> permission = permissions.getValue().stream().collect(Collectors.toList());
            permission.forEach(p -> {
                RoleXPermission rxp = new RoleXPermission();
                rxp.setRole(r);
                rxp.setPermission(p);
                rxp.setDeleted(0L);
                roleXPermissionApi.save(rxp);
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

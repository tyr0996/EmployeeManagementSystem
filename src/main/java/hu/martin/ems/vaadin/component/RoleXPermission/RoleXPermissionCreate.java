package hu.martin.ems.vaadin.component.RoleXPermission;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.service.PermissionService;
import hu.martin.ems.service.RoleService;
import hu.martin.ems.service.RoleXPermissionService;
import hu.martin.ems.vaadin.MainView;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "role_x_permission/create", layout = MainView.class)
public class RoleXPermissionCreate extends VerticalLayout {
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final RoleXPermissionService roleXPermissionService;

    public static Role staticRole;

    public RoleXPermissionCreate(RoleService roleService,
                                 PermissionService permissionService,
                                 RoleXPermissionService roleXPermissionService) {
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.roleXPermissionService = roleXPermissionService;
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

        if (staticRole != null) {
            roles.setValue(staticRole);
            permissions.setValue(roleXPermissionService.findAllPermission(staticRole));
            roleXPermissionService.clearPermissions(staticRole);
        }

        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> {
            Role r = roles.getValue();
            List<Permission> permission = permissions.getValue().stream().collect(Collectors.toList());
            permission.forEach(p -> {
                RoleXPermission rxp = new RoleXPermission();
                rxp.setRole(r);
                rxp.setPermission(p);
                rxp.setDeleted(0L);
                roleXPermissionService.saveOrUpdate(rxp);
            });
            staticRole = null;
            roles.clear();
            permissions.clear();
        });

        formLayout.add(roles, permissions, saveButton);
        add(formLayout);
    }
}

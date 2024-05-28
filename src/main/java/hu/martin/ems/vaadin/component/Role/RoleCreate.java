package hu.martin.ems.vaadin.component.Role;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Role;
import hu.martin.ems.service.RoleService;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.component.RoleXPermission.RoleXPermissionCreate;

@Route(value = "role/create", layout = MainView.class)
public class RoleCreate extends VerticalLayout {

    private final RoleService roleService;
    public static Role r;

    public RoleCreate(RoleService roleService) {
        this.roleService = roleService;
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");
        Button saveButton = new Button("Save");
        Button editRoleXPermissionButton = new Button("Edit permissions");

        if(r != null){
            nameField.setValue(r.getName());
        }

        editRoleXPermissionButton.addClickListener(event -> {
            RoleXPermissionCreate.staticRole = r;
            UI.getCurrent().navigate("role_x_permission/create");
        });

        saveButton.addClickListener(event -> {
            r = null;
            Role role = new Role();
            role.setName(nameField.getValue());
            role.setDeleted(0L);
            this.roleService.saveOrUpdate(role);
            Notification.show("Role saved: " + role.getName());
            nameField.clear();
        });

        formLayout.add(nameField, editRoleXPermissionButton, saveButton);
        add(formLayout);
    }
}

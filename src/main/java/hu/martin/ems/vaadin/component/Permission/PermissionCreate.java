package hu.martin.ems.vaadin.component.Permission;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Permission;
import hu.martin.ems.service.PermissionService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "permission/create", layout = MainView.class)
public class PermissionCreate extends VerticalLayout {

    public static Permission p;
    private final PermissionService permissionService;

    @Autowired
    public PermissionCreate(PermissionService permissionService) {
        this.permissionService = permissionService;
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");
        Button saveButton = new Button("Save");

        if (p != null) {
            nameField.setValue(p.getName());
        }

        saveButton.addClickListener(event -> {
            Permission permission = Objects.requireNonNullElseGet(p, Permission::new);
            permission.setDeleted(0L);
            permission.setName(nameField.getValue());
            this.permissionService.saveOrUpdate(permission);
            Notification.show("Permission saved: " + permission.getName());
            nameField.clear();
        });

        formLayout.add(nameField, saveButton);
        add(formLayout);
    }
}
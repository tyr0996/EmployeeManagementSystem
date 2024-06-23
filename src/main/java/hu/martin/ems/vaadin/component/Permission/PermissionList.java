package hu.martin.ems.vaadin.component.Permission;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Permission;
import hu.martin.ems.service.PermissionService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "permission/list", layout = MainView.class)
public class PermissionList extends VerticalLayout {

    private final PermissionService permissionService;
    private boolean showDeleted = false;
    private Grid<Permission> grid;

    @Autowired
    public PermissionList(PermissionService permissionService) {
        this.permissionService = permissionService;

        this.grid = new Grid<>(Permission.class);
        List<Permission> permissions = permissionService.findAll(false);
        this.grid.setItems(permissions);
        this.grid.setColumns("id", "name");

        this.grid.addComponentColumn(permission -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");

            editButton.addClickListener(event -> {
                PermissionCreate.p = permission;
                UI.getCurrent().navigate("permission/create");
            });

            restoreButton.addClickListener(event -> {
                this.permissionService.restore(permission);
                Notification.show("Permission restored: " + permission.getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.permissionService.delete(permission);
                Notification.show("Permission deleted: " + permission.getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.permissionService.permanentlyDelete(permission);
                Notification.show("Permission permanently deleted: " + permission.getName());
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (permission.getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (permission.getDeleted() == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setHeader("Options");

        // Toggle switch for showing deleted items
        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });

        add(showDeletedCheckbox, grid);
    }

    private void updateGridItems() {
        List<Permission> permissions = this.permissionService.findAll(showDeleted);
        this.grid.setItems(permissions);
    }
}
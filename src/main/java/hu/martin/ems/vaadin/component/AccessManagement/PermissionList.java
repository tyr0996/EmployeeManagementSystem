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
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.service.PermissionService;
import hu.martin.ems.service.RoleService;
import hu.martin.ems.service.RoleXPermissionService;
import hu.martin.ems.vaadin.component.Creatable;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/grid.css")
public class PermissionList extends VerticalLayout implements Creatable<Permission> {

    private final PermissionService permissionService;
    private final RoleService roleService;
    private final RoleXPermissionService roleXPermissionService;
    private boolean showDeleted = false;
    private PaginatedGrid<Permission, String> grid;
    private final PaginationSetting paginationSetting;

    public PermissionList(PermissionService permissionService,
                          RoleService roleService,
                          RoleXPermissionService roleXPermissionService,
                          PaginationSetting paginationSetting) {
        this.permissionService = permissionService;
        this.paginationSetting = paginationSetting;
        this.roleService = roleService;
        this.roleXPermissionService = roleXPermissionService;

        this.grid = new PaginatedGrid<>(Permission.class);
        List<Permission> permissions = permissionService.findAll(false);
        this.grid.setItems(permissions);
        this.grid.setColumns("id", "name");
        grid.addClassName("styling");
        grid.setPartNameGenerator(permission -> permission.getDeleted() != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        this.grid.addComponentColumn(permission -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(permission);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                this.permissionService.restore(permission);
                Notification.show("Permission restored: " + permission.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.permissionService.delete(permission);
                Notification.show("Permission deleted: " + permission.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.permissionService.permanentlyDelete(permission);
                Notification.show("Permission permanently deleted: " + permission.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    public void refreshGrid(){
        updateGridItems();
    }

    private void updateGridItems() {
        List<Permission> permissions = this.permissionService.findAll(showDeleted);
        this.grid.setItems(permissions);
    }

    @Override
    public Dialog getSaveOrUpdateDialog(Permission entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " permission");
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");

        MultiSelectComboBox<Role> roles = new MultiSelectComboBox<>("Roles");
        ComboBox.ItemFilter<Role> filterRole = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        roles.setItems(filterRole, roleService.findAll(false));
        roles.setItemLabelGenerator(Role::getName);

        Button saveButton = new Button("Save");

        if (entity != null) {
            nameField.setValue(entity.getName());
            roles.setValue(roleXPermissionService.findAllRole(entity));
        }

        saveButton.addClickListener(event -> {
            Permission permission = Objects.requireNonNullElseGet(entity, Permission::new);
            permission.setDeleted(0L);
            permission.setName(nameField.getValue());
            this.permissionService.saveOrUpdate(permission);
            this.roleXPermissionService.clearRoles(entity);
            roles.getSelectedItems().forEach(v ->{
                RoleXPermission rxp = new RoleXPermission(v, permission);
                rxp.setDeleted(0L);
                roleXPermissionService.saveOrUpdate(rxp);
            });
            Notification.show("Permission saved: " + permission.getName())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            createDialog.close();
        });

        formLayout.add(nameField, roles, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }
}
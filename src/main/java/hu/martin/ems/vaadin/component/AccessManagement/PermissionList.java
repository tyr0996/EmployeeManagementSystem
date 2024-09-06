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
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.RoleXPermissionApiClient;
import hu.martin.ems.vaadin.component.Creatable;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/grid.css")
@AnonymousAllowed
public class PermissionList extends VerticalLayout implements Creatable<Permission> {
    private boolean withDeleted = false;
    private PaginatedGrid<Permission, String> grid;
    private final PaginationSetting paginationSetting;
    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private final RoleXPermissionApiClient roleXPermissionApi = BeanProvider.getBean(RoleXPermissionApiClient.class);

    public PermissionList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(Permission.class);
        List<Permission> permissions = permissionApi.findAll();
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
                permissionApi.restore(permission);
                Notification.show("Permission restored: " + permission.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                permissionApi.delete(permission);
                Notification.show("Permission deleted: " + permission.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                permissionApi.permanentlyDelete(permission.getId());
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

        Checkbox withDeletedCheckbox = new Checkbox("Show deleted");
        withDeletedCheckbox.addValueChangeListener(event -> {
            withDeleted = event.getValue();
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(withDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, withDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }



    public void refreshGrid(){
        updateGridItems();
    }

    private void updateGridItems() {
        List<Permission> permissions = withDeleted ? permissionApi.findAllWithDeleted() : permissionApi.findAll();
        this.grid.setItems(permissions);
    }

    public Dialog getSaveOrUpdateDialog(Permission entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " permission");
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");

        MultiSelectComboBox<Role> roles = new MultiSelectComboBox<>("Roles");
        ComboBox.ItemFilter<Role> filterRole = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        List<Role> savedRoles = roleApi.findAll();
        roles.setItems(filterRole, savedRoles);
        roles.setItemLabelGenerator(Role::getName);

        Button saveButton = new Button("Save");

        if (entity != null) {
            nameField.setValue(entity.getName());
            List<Role> pairedRoles = roleXPermissionApi.findAllPairedRoleTo(entity);
            roles.setValue(pairedRoles);
        }

        saveButton.addClickListener(event -> {
            Permission permission = Objects.requireNonNullElseGet(entity, Permission::new);
            permission.setDeleted(0L);
            permission.setName(nameField.getValue());
            if(entity != null){
                permissionApi.save(permission);
            }
            else{
                permissionApi.update(permission);
            }
            roleXPermissionApi.removePermissionFromAllPaired(entity);
            roles.getSelectedItems().forEach(v ->{
                RoleXPermission rxp = new RoleXPermission(v, permission);
                rxp.setDeleted(0L);
                if(entity != null){
                    roleXPermissionApi.update(rxp);
                }
                else{
                    roleXPermissionApi.save(rxp);
                }
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
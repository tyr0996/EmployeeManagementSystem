package hu.martin.ems.vaadin.component.AccessManagement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import lombok.AllArgsConstructor;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;

@CssImport("./styles/grid.css")
public class RoleList extends VerticalLayout implements Creatable<Role> {

    private final RoleXPermissionService roleXPermissionService;
    private final PermissionService permissionService;
    private final RoleService roleService;
    private boolean showDeleted = false;
    private PaginatedGrid<GroupedRoleXPermission, String> grid;
    private final PaginationSetting paginationSetting;

    public RoleList(RoleXPermissionService roleXPermissionService,
                    RoleService roleService,
                    PermissionService permissionService,
                    PaginationSetting paginationSetting) {
        this.roleXPermissionService = roleXPermissionService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(GroupedRoleXPermission.class);
        grid.addClassName("styling");
        grid.setPartNameGenerator(roleXPermission -> roleXPermission.role.getDeleted() != 0 ? "deleted" : null);

        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        updateGridItems();

        this.grid.addColumn(GroupedRoleXPermission::roleAsString).setHeader("Role");
        this.grid.addColumn(GroupedRoleXPermission::permissionsAsString).setHeader("Permissions");

        this.grid.addComponentColumn(entry -> {
            Button editButton = new Button(EDIT.create());
            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(entry.role);
                dialog.open();
            });
            HorizontalLayout actions = new HorizontalLayout();
            actions.add(editButton);
            return actions;
        }).setHeader("Options");

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        HorizontalLayout hl = new HorizontalLayout();
        hl.add(create);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    public void refreshGrid(){
        updateGridItems();
    }

    private void updateGridItems() {
        List<RoleXPermission> rxps = roleXPermissionService.findAllWithUnused(false);
        Map<Role, List<Permission>> gridData = new HashMap<>();
        for (RoleXPermission rxp : rxps) {
            Role role = rxp.getRole();
            Permission permission = rxp.getPermission();
            if (!gridData.containsKey(role)) {
                gridData.put(role, new ArrayList<>());
            }
            if (!gridData.get(role).contains(permission)) {
                gridData.get(role).add(permission);
            }
        }

        List<GroupedRoleXPermission> data = new ArrayList<>();
        gridData.forEach((r, ps) ->
            data.add(new GroupedRoleXPermission(r, ps))
        );

        this.grid.setItems(data);
    }

    @Override
    public Dialog getSaveOrUpdateDialog(Role entity) {
        RoleXPermissionCreate rxpc = new RoleXPermissionCreate(roleService, permissionService, roleXPermissionService);
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " role");
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");
        Button saveButton = new Button("Save");
        //Button editRoleXPermissionButton = new Button("Edit permissions");

        MultiSelectComboBox<Permission> permissions = new MultiSelectComboBox<>("Permission");
        ComboBox.ItemFilter<Permission> filterPermission = (permission, filterString) ->
                permission.getName().toLowerCase().contains(filterString.toLowerCase());
        permissions.setItems(filterPermission, permissionService.findAll(false));
        permissions.setItemLabelGenerator(Permission::getName);

        if (entity != null) {
            nameField.setValue(entity.getName());
            permissions.setValue(roleXPermissionService.findAllPermission(entity));
        }

        saveButton.addClickListener(event -> {
            Role role = Objects.requireNonNullElseGet(entity, Role::new);
            role.setName(nameField.getValue());
            role.setDeleted(0L);
            this.roleService.saveOrUpdate(role);
            this.roleXPermissionService.clearPermissions(role);
            permissions.getSelectedItems().forEach(v ->{
                RoleXPermission rxp = new RoleXPermission(role, v);
                rxp.setDeleted(0L);
                roleXPermissionService.saveOrUpdate(rxp);
            });

            Notification.show((entity == null ? "Role saved: " : "Role updated: ") + role.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            createDialog.close();
            updateGridItems();
        });

        formLayout.add(nameField, permissions, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    @AllArgsConstructor
    protected class GroupedRoleXPermission {
        private Role role;
        private List<Permission> permissions;

        //Nem használhatjuk a get-es előtagot, mert akkor automatikusan hozzáadja a grid-hez, azt viszont nem szeretnénk
        public String permissionsAsString() {
            return permissions.stream()
                    .filter(Objects::nonNull)
                    .map(Permission::getName)
                    .collect(Collectors.joining(", "));
        }

        public String roleAsString() {
            return role.getName();
        }
    }
}
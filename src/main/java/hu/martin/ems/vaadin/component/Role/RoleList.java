package hu.martin.ems.vaadin.component.Role;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.service.RoleService;
import hu.martin.ems.service.RoleXPermissionService;
import hu.martin.ems.vaadin.MainView;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Route(value = "role/list", layout = MainView.class)
public class RoleList extends VerticalLayout {

    private final RoleXPermissionService roleXPermissionService;
    private final RoleService roleService;
    private boolean showDeleted = false;
    private Grid<GroupedRoleXPermission> grid;

    @Autowired
    public RoleList(RoleXPermissionService roleXPermissionService,
                    RoleService roleService) {
        this.roleXPermissionService = roleXPermissionService;
        this.roleService = roleService;

        this.grid = new Grid<>(GroupedRoleXPermission.class);
        List<RoleXPermission> rxps = roleXPermissionService.findAll();
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
        this.grid.addColumn(GroupedRoleXPermission::roleAsString).setHeader("Role");
        this.grid.addColumn(GroupedRoleXPermission::permissionsAsString).setHeader("Permissions");

        this.grid.addComponentColumn(entry -> {
            Button editButton = new Button("Edit");
            editButton.addClickListener(event -> {
                RoleCreate.r = entry.role;
                UI.getCurrent().navigate("role/create");
            });
            HorizontalLayout actions = new HorizontalLayout();
            actions.add(editButton);
            return actions;
        }).setHeader("Options");

        add(grid);
    }

    @AllArgsConstructor
    protected class GroupedRoleXPermission{
        private Role role;
        private List<Permission> permissions;

        //Nem használhatjuk a get-es előtagot, mert akkor automatikusan hozzáadja a grid-hez, azt viszont nem szeretnénk
        public String permissionsAsString(){
            return permissions.stream()
                              .filter(Objects::nonNull)
                              .map(Permission::getName)
                              .collect(Collectors.joining(", "));
        }

        public String roleAsString(){
            return role.getName();
        }
    }
}
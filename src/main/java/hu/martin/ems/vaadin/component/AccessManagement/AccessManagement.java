package hu.martin.ems.vaadin.component.AccessManagement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.service.PermissionService;
import hu.martin.ems.service.RoleService;
import hu.martin.ems.service.RoleXPermissionService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "accessManagement/list", layout = MainView.class)
public class AccessManagement extends VerticalLayout {

    private RoleList roleList;
    private PermissionList permissionList;
    private RoleXPermissionCreate roleXPermissionCreate;
    private final PaginationSetting paginationSetting;
    @Autowired
    public AccessManagement(RoleXPermissionService roleXPermissionService,
                            RoleService roleService,
                            PermissionService permissionService,
                            PaginationSetting paginationSetting) {
        this.roleList = new RoleList(roleXPermissionService, roleService, permissionService, paginationSetting);
        this.permissionList = new PermissionList(permissionService, roleService, roleXPermissionService, paginationSetting);
        this.roleXPermissionCreate = new RoleXPermissionCreate(roleService, permissionService, roleXPermissionService);
        this.paginationSetting = paginationSetting;
        HorizontalLayout buttons = new HorizontalLayout();
        Button role = new Button("Roles");
        Button permission = new Button("Permissions");
        Button rolePermissionPairing = new Button("Role-permission pairing");
        buttons.add(role, permission, rolePermissionPairing);

        role.addClickListener(event -> {
            this.roleList.refreshGrid();
            removeAll();
            add(buttons, roleList);
        });

        permission.addClickListener(event -> {
            this.permissionList.refreshGrid();
            removeAll();
            add(buttons, permissionList);
        });

        rolePermissionPairing.addClickListener(event -> {
            removeAll();
            add(buttons, roleXPermissionCreate);
        });
        add(buttons);
    }
}
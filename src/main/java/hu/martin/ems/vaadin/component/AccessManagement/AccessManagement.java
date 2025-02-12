package hu.martin.ems.vaadin.component.AccessManagement;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.vaadin.MainView;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "accessManagement/list", layout = MainView.class)
@RolesAllowed("ROLE_AccessManagementMenuOpenPermission")
//@Secured("ROLE_AccessManagementMenuOpenPermission")
@NeedCleanCoding
public class AccessManagement extends VerticalLayout implements RouterLayout {

    private final PaginationSetting paginationSetting;

    private Button listRoles;
    private Button listPermissions;
    private Button pairRolesWithPermissions;
    private HorizontalLayout buttonsLayout;
//    private MainView mainView;

    @Autowired
    public AccessManagement(PaginationSetting paginationSetting) {
//        this.mainView = mainView;
        this.paginationSetting = paginationSetting;
//        this.roleList = new RoleList(paginationSetting);
//        this.permissionList = new PermissionList(paginationSetting);
//        this.roleXPermissionCreate = new RoleXPermissionCreate();
        this.buttonsLayout = createButtonRow();
        add(buttonsLayout);
    }

    public HorizontalLayout createButtonRow(){
        HorizontalLayout hl = new HorizontalLayout();

        Button listRoles = new Button("Roles");
        listRoles.setId("AccessManagement_RolesButton");
        Button listPermissions = new Button("Permissions");
        listPermissions.setId("AccessManagement_Permissions_button");
        Button pairRolesWithPermissions = new Button("Role-permission pairing");
        pairRolesWithPermissions.setId("AccessManagement_RolePermissionPairingButton");

        listRoles.addClickListener(event -> refreshLayout(RoleList.class));
        listPermissions.addClickListener(event -> refreshLayout(PermissionList.class));
        pairRolesWithPermissions.addClickListener(event -> refreshLayout(RoleXPermissionCreate.class));

        hl.add(listRoles, listPermissions, pairRolesWithPermissions);
        return hl;
    }

    private void refreshLayout(Class<? extends Component> c){
        removeAll();
        UI.getCurrent().navigate(c);
//        add(this.buttonsLayout, c);
    }
}
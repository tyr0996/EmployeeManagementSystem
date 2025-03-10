package hu.martin.ems.vaadin.component.AccessManagement;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.auth.SecurityService;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.model.User;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.PermissionApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@AnonymousAllowed
@NeedCleanCoding
@RolesAllowed("ROLE_RoleXPermissionMenuOpenPermission")
@Route(value = "/accessManagement/list/roleXPermission", layout = MainView.class)
public class RoleXPermissionCreate extends AccessManagement {

    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private final PermissionApiClient permissionApi = BeanProvider.getBean(PermissionApiClient.class);
    private final SecurityService securityService = BeanProvider.getBean(SecurityService.class);
    private final UserApiClient userApiClient = BeanProvider.getBean(UserApiClient.class);
    List<Role> roleList;
    List<Permission> permissionList;
    Button saveButton;
    Logger logger = LoggerFactory.getLogger(RoleXPermissionCreate.class);
//    private MainView mainView;

    @Autowired
    public RoleXPermissionCreate(PaginationSetting paginationSetting) {
        super(paginationSetting);
        add(buildFormLayout());
    }

    public FormLayout buildFormLayout() {
        FormLayout formLayout = new FormLayout();

        ComboBox<Role> roleComboBox = createRoleComboBox();
        MultiSelectComboBox<Permission> permissionComboBox = createPermissionComboBox(roleComboBox);
        saveButton = createSaveButton(roleComboBox, permissionComboBox);

        formLayout.add(roleComboBox, permissionComboBox, saveButton);
        return formLayout;
    }

    private ComboBox<Role> createRoleComboBox() {
        setupRoles();
        ComboBox<Role> roleComboBox = new ComboBox<>("Role");
        if(roleList != null){
            roleComboBox.setItems((role, filter) -> role.getName().toLowerCase().contains(filter.toLowerCase()), roleList);
            roleComboBox.setItemLabelGenerator(Role::getName);
        }
        else{
            roleComboBox.setErrorMessage("Error happened while getting roles");
            roleComboBox.setEnabled(false);
            roleComboBox.setInvalid(true);
        }

        return roleComboBox;
    }

    private void setupRoles() {
        EmsResponse response = roleApi.findAll();
        switch (response.getCode()){
            case 200:
                roleList = (List<Role>) response.getResponseData();
                EmsResponse e = userApiClient.findByUsername(securityService.getAuthenticatedUser().getUsername());
                switch (e.getCode()){
                    case 200: {
                        User loggedInUser = (User) e.getResponseData();
                        roleList.remove(loggedInUser.getRoleRole());
                        break;
                    }
                    default: {
                        roleList = null;
                        logger.error("Can't get logged in User object");
                        Notification.show("Can't get logged in User object").addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                break;
            default:
                roleList = null;
                logger.error("Role findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private MultiSelectComboBox<Permission> createPermissionComboBox(ComboBox<Role> roleComboBox) {
        setupPermissions();
        MultiSelectComboBox<Permission> permissionComboBox = new MultiSelectComboBox<>("Permission");
        if(permissionList != null){
            permissionComboBox.setItems((permission, filter) -> permission.getName().toLowerCase().contains(filter.toLowerCase()), permissionList);
            permissionComboBox.setItemLabelGenerator(Permission::getName);

            roleComboBox.addValueChangeListener(event -> {
                Role selectedRole = event.getValue();
                List<Permission> permissions = selectedRole.getPermissions().stream().toList();
                permissionComboBox.setValue(permissions);
            });
        }
        else{
            permissionComboBox.setInvalid(true);
            permissionComboBox.setEnabled(false);
            permissionComboBox.setErrorMessage("Error happened while getting permissions");
        }

        return permissionComboBox;
    }

    private void setupPermissions() {
        EmsResponse response = permissionApi.findAll();
        switch (response.getCode()){
            case 200:
                permissionList = (List<Permission>) response.getResponseData();
                break;
            default:
                permissionList = null;
                logger.error("Permission findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private Button createSaveButton(ComboBox<Role> roleComboBox, MultiSelectComboBox<Permission> permissionComboBox) {
        Button saveButton = new Button("Save");
        if(roleList == null || permissionList == null){
            saveButton.setEnabled(false);
        }

        saveButton.addClickListener(event -> saveRolePermissions(roleComboBox, permissionComboBox));

        return saveButton;
    }

    private void saveRolePermissions(ComboBox<Role> roleComboBox, MultiSelectComboBox<Permission> permissionComboBox) {
        Role selectedRole = roleComboBox.getValue();
        Set<Permission> selectedPermissions = permissionComboBox.getValue();
        selectedRole.setPermissions(selectedPermissions);
        EmsResponse response = roleApi.update(selectedRole);
//
//        roleXPermissionApi.removeAllPermissionsFrom(selectedRole);
//
//        Boolean success = true;
//        for(int i = 0; i < selectedPermissions.size(); i++){
//            Permission permission = selectedPermissions.get(i);
//            RoleXPermission roleXPermission = new RoleXPermission(selectedRole, permission);
//            roleXPermission.setDeleted(0L);
//            EmsResponse response = roleXPermissionApi.save(roleXPermission);
//            switch (response.getCode()){
//                case 200: break;
//                case 500: Notification.show("Role-permission pairing failed").addThemeVariants(NotificationVariant.LUMO_ERROR); return;
//            }
//        }
//        clearForm(roleComboBox, permissionComboBox);
        switch (response.getCode()){
            case 200:{
                Notification.show("Role successfully paired!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                break;
            }
            default: {
                Notification.show("Role pairing failed!").addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
            }
        }
    }
}

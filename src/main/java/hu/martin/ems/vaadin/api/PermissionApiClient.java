package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;


@Component
@NeedCleanCoding
public class PermissionApiClient extends EmsApiClient<Permission> {
    public PermissionApiClient() {
        super(Permission.class);
    }

    @Autowired
    RoleApiClient roleApiClient;

    @Override
    public EmsResponse findAllWithDeleted() {
        EmsResponse allPermisssionResponse = super.findAllWithDeleted();
        if(allPermisssionResponse.getCode() != 200){
            return allPermisssionResponse;
        }
        List<Permission> permissions = (List<Permission>) allPermisssionResponse.getResponseData();
        EmsResponse allRoleResponse = roleApiClient.findAll();
        if(allRoleResponse.getCode() != 200){
            return allRoleResponse;
        }
        List<Role> allRole = (List<Role>) allRoleResponse.getResponseData();
        permissions.forEach(p -> {
            HashSet<Role> roles = new HashSet<>();
            allRole.forEach(r -> {
//                if (p.getRoleIds().contains(r.id)) {
//                    roles.add(r);
//                }
            });
            p.setRoles(roles);
        });
        return new EmsResponse(200, permissions, "");
    }
}

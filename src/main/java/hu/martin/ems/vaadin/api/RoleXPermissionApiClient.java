package hu.martin.ems.vaadin.api;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@NeedCleanCoding
public class RoleXPermissionApiClient extends EmsApiClient<RoleXPermission> {
    public RoleXPermissionApiClient() {
        super(RoleXPermission.class);
    }

    public List<Role> findAllPairedRoleTo(Permission p){
        //TODO
        return new ArrayList<>();
    }

    public List<Permission> findAllPairedPermissionsTo(Role r){
        //TODO
        return new ArrayList<>();
    }

    public List<RoleXPermission> findAllWith(Boolean withDeleted){
        //TODO
        return new ArrayList<>();
    }

    public void removePermissionFromAllPaired(Permission p){
        //TODO
    }

    public void removeAllPermissionsFrom(Role r){
        //TODO
    }
}

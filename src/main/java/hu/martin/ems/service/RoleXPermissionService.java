package hu.martin.ems.service;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.repository.RoleXPermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@NeedCleanCoding
public class RoleXPermissionService extends BaseService<RoleXPermission, RoleXPermissionRepository> {

    public RoleXPermissionService(RoleXPermissionRepository roleXPermissionRepository) {
        super(roleXPermissionRepository);
    }

    public List<RoleXPermission> findAllWithUnused(Boolean withDeleted){
        return this.repo.findAllWithUnused(withDeleted);
    }

    public List<Permission> findAllPermission(Long roleId) {
        return this.repo.findAllPermission(roleId);
    }

    public List<Role> findAllRole(Long permissionId) {
        return this.repo.findAllRole(permissionId);
    }

    public void removeAllPermissionsFrom(Role r) { this.repo.removeAllPermissionsFrom(r.getId()); }

    public void removeAllRolesFrom(Permission p) {
        this.repo.removeAllRolesFrom(p.id);
    }
}

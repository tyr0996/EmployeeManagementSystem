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

    public List<Permission> findAllPermission(Role r) { return this.repo.findAllPermission(r.getId()); }

    public List<Role> findAllRole(Permission p) {
        return this.repo.findAllRole(p.getId());
    }

    public void clearPermissions(Role r) { this.repo.clearPermissions(r.getId()); }

    public void clearRoles(Permission p) {
        this.repo.clearRoles(p.id);
    }
}

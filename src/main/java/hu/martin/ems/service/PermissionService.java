package hu.martin.ems.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Permission;
import hu.martin.ems.repository.PermissionRepository;
import org.springframework.stereotype.Service;

@Service
@NeedCleanCoding
public class PermissionService extends BaseService<Permission, PermissionRepository> {

    public PermissionService(PermissionRepository permissionRepository) {
        super(permissionRepository);
    }
}
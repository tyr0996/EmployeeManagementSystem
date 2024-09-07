package hu.martin.ems.service;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Role;
import hu.martin.ems.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@NeedCleanCoding
public class RoleService extends BaseService<Role, RoleRepository> {
    public RoleService(RoleRepository roleRepository) {
        super(roleRepository);
    }
}

package hu.martin.ems.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Role;
import hu.martin.ems.repository.RoleRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
@NeedCleanCoding
public class RoleService extends BaseService<Role, RoleRepository> {
    public RoleService(RoleRepository roleRepository) {
        super(roleRepository);
    }

    public Role getNoRole() { return this.repo.getNoRole().orElseThrow(() -> new EntityNotFoundException("Cann't find NO_ROLE"));}
}

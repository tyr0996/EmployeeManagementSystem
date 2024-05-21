package hu.martin.core.service;

import hu.martin.core.model.Role;
import hu.martin.core.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;

    public Role saveEmployee(Role employee){
        return roleRepository.save(employee);
    }
}

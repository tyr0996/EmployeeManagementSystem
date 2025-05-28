package hu.martin.ems.controller;

import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Role;
import hu.martin.ems.repository.RoleRepository;
import hu.martin.ems.service.RoleService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/role")
public class RoleController extends BaseController<Role, RoleService, RoleRepository> {
    public RoleController(RoleService service) {
        super(service);
    }
}


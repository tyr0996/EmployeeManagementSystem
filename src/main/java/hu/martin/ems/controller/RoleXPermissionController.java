package hu.martin.ems.controller;

import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.repository.RoleXPermissionRepository;
import hu.martin.ems.service.RoleXPermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/roleXPermission")
public class RoleXPermissionController extends BaseController<RoleXPermission, RoleXPermissionService, RoleXPermissionRepository> {
    public RoleXPermissionController(RoleXPermissionService service) {
        super(service);
    }
}

package hu.martin.ems.controller;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Permission;
import hu.martin.ems.repository.PermissionRepository;
import hu.martin.ems.service.PermissionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/permission")
@NeedCleanCoding
public class PermissionController extends BaseController<Permission, PermissionService, PermissionRepository> {

    public PermissionController(PermissionService service) {
        super(service);
    }
}

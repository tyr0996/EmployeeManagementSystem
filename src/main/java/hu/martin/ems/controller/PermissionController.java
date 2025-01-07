package hu.martin.ems.controller;

import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Permission;
import hu.martin.ems.repository.PermissionRepository;
import hu.martin.ems.service.PermissionService;
import hu.martin.ems.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/permission")
public class PermissionController extends BaseController<Permission, PermissionService, PermissionRepository> {

    public PermissionController(PermissionService service) {
        super(service);
    }

    @Autowired
    private RoleService roleService;

//    @Override
//    @PostMapping(path = "/save", produces = StaticDatas.Produces.JSON)
//    public ResponseEntity<String> save(@RequestBody Permission entity) {
//        entity.setRoles(new HashSet<>(roleService.findAllByIds(entity.getRoleIds())));
//        return new ResponseEntity<>(gson.toJson(service.save(entity)), HttpStatus.OK);
//    }
//
//    @Override
//    @PutMapping(path = "/update", produces = StaticDatas.Produces.JSON)
//    public ResponseEntity<String> update(@RequestBody Permission entity) {
//        entity.setRoles(new HashSet<>(roleService.findAllByIds(entity.getRoleIds())));
//        return new ResponseEntity<>(gson.toJson(service.update(entity)), HttpStatus.OK);
//    }
}

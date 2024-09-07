package hu.martin.ems.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.repository.RoleXPermissionRepository;
import hu.martin.ems.service.RoleXPermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/roleXPermission")
public class RoleXPermissionController extends BaseController<RoleXPermission, RoleXPermissionService, RoleXPermissionRepository> {
    public RoleXPermissionController(RoleXPermissionService service) {
        super(service);
    }

    public ResponseEntity<String> findAllPairedRoleTo(Permission p) throws JsonProcessingException {
        return new ResponseEntity<>(om.writeValueAsString(service.findAllRole(p)), HttpStatus.OK);
    }

    public ResponseEntity<String> findAllPairedPermissionsTo(Role r) throws JsonProcessingException {
        return new ResponseEntity<>(om.writeValueAsString(service.findAllPermission(r)), HttpStatus.OK);
    }

    public ResponseEntity<String> findAllWithUnused(Boolean withUnused) throws JsonProcessingException {
        return new ResponseEntity<>(om.writeValueAsString(service.findAllWithUnused(withUnused)), HttpStatus.OK);
    }

    public ResponseEntity<String> removePermissionFromAllPaired(Permission p) throws JsonProcessingException {
        //return new ResponseEntity<>(om.writeValueAsString(service.clearRoles(p)), HttpStatus.OK);
        //TODO
        return null;
    }

    public ResponseEntity<String> removeAllPermissionsFrom(Role r){
        //return new ResponseEntity<>(om.writeValueAsString(service.clearPermissions(r)), HttpStatus.OK);
        //TODO
        return null;
    }
}

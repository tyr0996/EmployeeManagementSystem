//package hu.martin.ems.controller;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import hu.martin.ems.annotations.NeedCleanCoding;
//import hu.martin.ems.core.controller.BaseController;
//import hu.martin.ems.model.Permission;
//import hu.martin.ems.model.Role;
//import hu.martin.ems.model.RoleXPermission;
//import hu.martin.ems.repository.RoleXPermissionRepository;
//import hu.martin.ems.service.RoleXPermissionService;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/api/roleXPermission")
//@NeedCleanCoding
//public class RoleXPermissionController extends BaseController<RoleXPermission, RoleXPermissionService, RoleXPermissionRepository> {
//    public RoleXPermissionController(RoleXPermissionService service) {
//        super(service);
//    }
//
//    @GetMapping(path = "findAllPairedRoleTo")
//    public ResponseEntity<String> findAllPairedRoleTo(@RequestParam Long permissionId) throws JsonProcessingException {
//        return new ResponseEntity<>(gson.toJson(service.findAllRole(permissionId)), HttpStatus.OK);
//    }
//
//    @GetMapping(path = "findAllPairedPermissionsTo")
//    public ResponseEntity<String> findAllPairedPermissionsTo(@RequestParam Long roleId) throws JsonProcessingException {
//        return new ResponseEntity<>(gson.toJson(service.findAllPermission(roleId)), HttpStatus.OK);
//    }
//
//    //TODO a sima withUsed nem használjuk
//    @GetMapping(path = "findAllWithUnused")
//    public ResponseEntity<String> findAllWithUnused() throws JsonProcessingException {
//        List<RoleXPermission> rxps = service.findAllWithUnusedWithDeleted();
//        return new ResponseEntity<>(gson.toJson(rxps), HttpStatus.OK);
//    }
//
//    @PutMapping(path = "removeAllRolesFrom")
//    public ResponseEntity<String> removeAllRolesFrom(@RequestBody Permission p) {
//        service.removeAllRolesFrom(p);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    @PutMapping(path = "removeAllPermissionsFrom")
//    public ResponseEntity<String> removeAllPermissionsFrom(@RequestBody Role r) {
//        service.removeAllPermissionsFrom(r);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    @GetMapping(path = "findAlRoleXPermissionByRole")
//    public ResponseEntity<String> findAlRoleXPermissionByRole(Long roleId) throws JsonProcessingException {
//        return new ResponseEntity<>(gson.toJson(service.findAlRoleXPermissionByRole(roleId)), HttpStatus.OK);
//    }
//
////    @GetMapping(path = "findAllRoleXPermissionByPermission")
////    public ResponseEntity<String> findAllRoleXPermissionByPermission(Long permissionId) throws JsonProcessingException {
////        return new ResponseEntity<>(gson.toJson(service.findAlRoleXPermissionByPermission(permissionId)), HttpStatus.OK);
////    }
//}

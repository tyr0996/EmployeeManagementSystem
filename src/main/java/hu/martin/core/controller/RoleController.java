package hu.martin.core.controller;

import hu.martin.core.model.Role;
import hu.martin.core.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping
    public ResponseEntity<Role> createEmployee(@RequestBody Role role) {
        Role savedRole = roleService.saveEmployee(role);
        return ResponseEntity.ok(savedRole);
    }
}
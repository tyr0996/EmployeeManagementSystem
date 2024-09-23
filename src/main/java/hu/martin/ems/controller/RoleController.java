package hu.martin.ems.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Role;
import hu.martin.ems.repository.RoleRepository;
import hu.martin.ems.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/role")
public class RoleController extends BaseController<Role, RoleService, RoleRepository> {

    public RoleController(RoleService service) {
        super(service);
    }

    @GetMapping(path = "/findByName")
    public ResponseEntity<String> findByName(@RequestParam String name) throws JsonProcessingException {
        return new ResponseEntity<>(om.writeValueAsString(this.service.findByName(name)), HttpStatus.OK);
    }
}


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
        Role r = this.service.findByName(name);
        if(r != null){
            return new ResponseEntity<>(gson.toJson(r), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/findByNameWithNegativeId")
    public ResponseEntity<String> findByNameWithNegativeId(@RequestParam String name) {
        Role r = this.service.findByNameWithNegativeId(name);
        if(r != null){
            return new ResponseEntity<>(gson.toJson(r), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


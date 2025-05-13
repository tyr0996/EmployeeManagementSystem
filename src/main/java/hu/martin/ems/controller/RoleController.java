package hu.martin.ems.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Role;
import hu.martin.ems.repository.RoleRepository;
import hu.martin.ems.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/role")
public class RoleController extends BaseController<Role, RoleService, RoleRepository> {

    public RoleController(RoleService service) {
        super(service);
    }

    @AnonymousAllowed
    @GetMapping(path = "/getNoRole")
    public ResponseEntity<String> getNoRole(){
        Role noRole = service.getNoRole();
        if(noRole != null){
            return new ResponseEntity<>(gson.toJson(noRole), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("Can't find NO_ROLE role.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}


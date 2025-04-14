package hu.martin.ems.core.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.model.User;
import hu.martin.ems.core.repository.UserRepository;
import hu.martin.ems.core.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
//@AnonymousAllowed
@RequestMapping("/api/user")
public class UserController extends BaseController<User, UserService, UserRepository> {
    public UserController(UserService service) {
        super(service);
    }

    @AnonymousAllowed
    @GetMapping(path = "/getByUsername", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getByUsername(@RequestParam String username) {
        return new ResponseEntity<>(gson.toJson(service.findByUsername(username)), HttpStatus.OK);
    }
}

package hu.martin.ems.core.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.User;
import hu.martin.ems.core.repository.UserRepository;
import hu.martin.ems.core.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Controller
//@AnonymousAllowed
@RequestMapping("/api/user")
public class UserController extends BaseController<User, UserService, UserRepository> {
    public UserController(UserService service) {
        super(service);
    }

    @AnonymousAllowed
    @GetMapping(path = "/getByUsername", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> getByUsername(@RequestParam String username) {
        try{
            User user = service.findByUsername(username);
            return new ResponseEntity<>(gson.toJson(user), HttpStatus.OK);
        }
        catch (WebClientResponseException ex){
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/userExists", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> userExists(@RequestParam String username) {
        try{
            User user = service.userExists(username);
            return new ResponseEntity<>(gson.toJson(user), HttpStatus.OK);
        }
        catch (WebClientResponseException ex){
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

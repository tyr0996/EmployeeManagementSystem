package hu.martin.ems.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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

@Controller
@RequestMapping("/api/user")
public class UserController extends BaseController<User, UserService, UserRepository> {
    public UserController(UserService service) {
        super(service);
    }

    @GetMapping(path = "/getByUsername", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> getByUsername(@RequestParam String username) {
        User user = service.findByUsername(username);
        try{
            return new ResponseEntity<>(om.writeValueAsString(user), HttpStatus.OK);
        }
        catch(JsonProcessingException e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

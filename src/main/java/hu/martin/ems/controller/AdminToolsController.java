package hu.martin.ems.controller;

import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.service.AdminToolsService;
import jakarta.annotation.security.PermitAll;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.InvocationTargetException;

@Controller
@RequestMapping("/api/adminTools")
@PermitAll
//@AnonymousAllowed
@Slf4j
public class AdminToolsController {

    @Setter
    private AdminToolsService adminToolsService;

    @Autowired
    public AdminToolsController(AdminToolsService adminToolsService){
        this.adminToolsService = adminToolsService;
    }

    @DeleteMapping(path = "/clearDatabase")
    public ResponseEntity<String> clearDatabase() {
        try{
            adminToolsService.clearAllDatabaseTable();
            return new ResponseEntity<>(EmsResponse.Description.CLEAR_DATABASE_SUCCESS, HttpStatus.OK);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            return new ResponseEntity<>(EmsResponse.Description.CLEAR_DATABASE_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

package hu.martin.ems.controller;

import com.google.gson.Gson;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.service.AdminToolsService;
import hu.martin.ems.vaadin.api.Error;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;

@Controller
@RequestMapping("/api/adminTools")
@PermitAll
//@AnonymousAllowed
@Slf4j
public class AdminToolsController {
    private AdminToolsService adminToolsService;

    private Gson gson;

    @Autowired
    public AdminToolsController(AdminToolsService adminToolsService,
                                Gson gson){
        this.adminToolsService = adminToolsService;
        this.gson = gson;
    }

    @DeleteMapping(path = "/clearDatabase", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> clearDatabase() {
        try{
            adminToolsService.clearAllDatabaseTable();
            return new ResponseEntity<>(EmsResponse.Description.CLEAR_DATABASE_SUCCESS, HttpStatus.OK);
        } catch (ClassNotFoundException e) {
            return new ResponseEntity<>(gson.toJson(new Error(Instant.now().toEpochMilli(), 500, EmsResponse.Description.CLEAR_DATABASE_FAILED, "/api/adminTools/clearDatabase")), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

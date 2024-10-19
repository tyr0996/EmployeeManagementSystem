package hu.martin.ems.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.service.AdminToolsService;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

@Controller
@RequestMapping("/api/adminTools")
@AnonymousAllowed
@NoArgsConstructor
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

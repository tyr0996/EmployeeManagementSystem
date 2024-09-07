package hu.martin.ems.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.repository.CodeStoreRepository;
import hu.martin.ems.service.CodeStoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/codeStore")
@AnonymousAllowed
public class CodeStoreController extends BaseController<CodeStore, CodeStoreService, CodeStoreRepository> {
    public CodeStoreController(CodeStoreService service) {
        super(service);
    }

    @GetMapping(path = "/getChildren")
    public ResponseEntity<String> getChildren(@RequestParam(value = "parentCodeStoreId") Long parentCodeStoreId) throws JsonProcessingException {
        return new ResponseEntity<>(om.writeValueAsString(service.getChildren(parentCodeStoreId)), HttpStatus.OK);
    }

    @GetMapping(path = "/getByName")
    public ResponseEntity<String> getByName(@RequestParam(value = "name") String name) throws JsonProcessingException {
        return new ResponseEntity<>(om.writeValueAsString(service.findByName(name)), HttpStatus.OK);
    }
}

package hu.martin.ems.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.service.EmailSendingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/emailSending")
@AnonymousAllowed
@NeedCleanCoding
public class EmailSendingController {
    private EmailSendingService service;

    public EmailSendingController(EmailSendingService service) {
        this.service = service;
    }

    private final Gson gson = BeanProvider.getBean(Gson.class);


    @PostMapping(path = "/sendEmail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> send(@RequestBody EmailProperties properties) throws JsonProcessingException {
        return new ResponseEntity<>(gson.toJson(service.send(properties)), HttpStatus.OK);
    }
}

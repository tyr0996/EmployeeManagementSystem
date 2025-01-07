package hu.martin.ems.core.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.service.EmailSendingService;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(path = "/api/emailSending")
@AnonymousAllowed
@NeedCleanCoding
public class EmailSendingController {
    @Setter
    private EmailSendingService service;

    public EmailSendingController(EmailSendingService service){
        this.service = service;
    }

    private final Gson gson = BeanProvider.getBean(Gson.class);


    @PostMapping(path = "/sendEmail", consumes = StaticDatas.Consumes.JSON)
    public ResponseEntity<String> send(@RequestBody EmailProperties properties) throws JsonProcessingException {
        boolean success = service.send(properties);
        return new ResponseEntity<>(gson.toJson(success), HttpStatus.OK);
    }
}

package hu.martin.ems.vaadin.api;

import hu.martin.ems.core.model.EmailAttachment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmailSendingApi {

    public void send(String to, String messageContent, String subject, List<EmailAttachment> attachments){
        //TODO
    }
}

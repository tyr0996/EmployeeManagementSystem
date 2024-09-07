package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.model.EmailProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@NeedCleanCoding
public class EmailSendingApi {

    protected final WebClient webClient;
    private final ObjectMapper om = new ObjectMapper();

    public EmailSendingApi(){
        this.webClient = WebClient.builder().baseUrl("http://localhost:8080/api/emailSending/").build();
    }

    public Boolean send(EmailProperties emailProperties) {
        try{
            String response =  webClient.post()
                    .uri("sendEmail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(emailProperties))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return Boolean.valueOf(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
}

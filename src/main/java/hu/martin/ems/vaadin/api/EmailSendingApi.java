package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.model.EmailProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@NeedCleanCoding
@Lazy
public class EmailSendingApi {

    protected final WebClient webClient;
    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    public EmailSendingApi(){
        this.webClient = WebClient.builder().baseUrl("http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/emailSending/").build();
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

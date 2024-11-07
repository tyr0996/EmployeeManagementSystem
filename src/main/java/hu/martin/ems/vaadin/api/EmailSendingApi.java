package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.JacksonConfig;
import hu.martin.ems.core.model.EmailProperties;
import hu.martin.ems.core.model.EmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;

@Service
@Slf4j
@Lazy
@NeedCleanCoding
public class EmailSendingApi {

    protected WebClient webClient;
    private final ObjectMapper om = new JacksonConfig().objectMapper();

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @PostConstruct
    public void init(){
        this.webClient = WebClient.builder().baseUrl("http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/emailSending/").build();
    }

    public EmsResponse send(EmailProperties emailProperties) {
        try{
            String response =  webClient.post()
                    .uri("sendEmail")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(emailProperties))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(Boolean.valueOf(response)){
                return new EmsResponse(200, true, "");
            }
            else{
                return new EmsResponse(200, false, "Email sending failed");
            }
        } catch (JsonProcessingException e) {
            return new EmsResponse(500, false, "JsonProcessingException");
        }
    }
}

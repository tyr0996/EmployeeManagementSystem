package hu.martin.ems.vaadin.api;

import com.google.gson.Gson;
import hu.martin.ems.annotations.NeedCleanCoding;
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

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
    private Gson gson;

    @PostConstruct
    public void init(){
        this.webClient = WebClient.builder().baseUrl("http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/emailSending/").build();
    }

    public EmsResponse send(EmailProperties emailProperties) {
        String response =  webClient.post()
                .uri("sendEmail")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(gson.toJson(emailProperties))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if(Boolean.valueOf(response)){
            return new EmsResponse(200, "Email sent!");
        }
        else{
            return new EmsResponse(500, "Email sending failed");
        }
    }
}

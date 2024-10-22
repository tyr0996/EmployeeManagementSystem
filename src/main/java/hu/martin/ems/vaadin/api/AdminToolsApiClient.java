package hu.martin.ems.vaadin.api;

import hu.martin.ems.core.model.EmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class AdminToolsApiClient {
    protected WebClient webClient;

    private WebClient.Builder webClientBuilder;

    private Logger logger = LoggerFactory.getLogger(AdminToolsApiClient.class);


    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    public AdminToolsApiClient(){
        this.webClientBuilder = WebClient.builder();
    }

    public EmsResponse clearDatabase(){
        initWebClient();
        try{
            String repsonse = webClient.delete()
                    .uri("clearDatabase")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, repsonse);
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }

    }

    public void initWebClient(){
        if(webClient == null){
            String baseUrl = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/" + "adminTools" + "/";
            webClient = webClientBuilder.baseUrl(baseUrl).build();
        }
    }
}

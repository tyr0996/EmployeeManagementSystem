package hu.martin.ems.vaadin.api;

import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.vaadin.api.base.WebClientProvider;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayInputStream;

@Service
@Slf4j
public class AdminToolsApiClient {
    private WebClient.Builder webClientBuilder;

    private Logger logger = LoggerFactory.getLogger(AdminToolsApiClient.class);

    @Autowired
    private WebClientProvider webClientProvider;

    public AdminToolsApiClient(){
        this.webClientBuilder = WebClient.builder();
    }

    private static final String entityName = "adminTools";

    public EmsResponse clearDatabase(){
        WebClient csrfWebClient = webClientProvider.initCsrfWebClient(entityName);
        try{
            String repsonse = csrfWebClient.delete()
                    .uri("clearDatabase")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, repsonse);
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString()); //TODO Errorosítás
        }
    }

    public EmsResponse exportApis(){
        WebClient webClient = webClientProvider.initWebClient("eps");
        try{
            byte[] response =  webClient.get()
                    .uri("exportApis")
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return new EmsResponse(200, new ByteArrayInputStream(response), "");
        }
        catch (WebClientResponseException ex){
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
        }
    }
}

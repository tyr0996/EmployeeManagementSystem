package hu.martin.ems.vaadin.api;

import com.google.gson.Gson;
import hu.martin.ems.core.actuator.HealthStatusResponse;
import hu.martin.ems.core.actuator.MappingsResult;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.vaadin.api.base.WebClientProvider;
import hu.martin.ems.vaadin.core.EmsError;
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

    @Autowired
    public Gson gson;

    private Logger logger = LoggerFactory.getLogger(AdminToolsApiClient.class);

    @Autowired
    private WebClientProvider webClientProvider;

    public AdminToolsApiClient() {
        this.webClientBuilder = WebClient.builder();
    }

    private static final String entityName = "adminTools";

    public EmsResponse healthStatus() {
        WebClient webClient = webClientProvider.initBaseUrlWebClient();
        try {
            String repsonse = webClient.get()
                    .uri("actuator/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, repsonse, "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(),
                    gson.toJson(new HealthStatusResponse("Inaccessible")),
                    null);
        }
    }

    public EmsResponse exportApis() {
        WebClient webClient = webClientProvider.initBaseUrlWebClient();
        try {
            String repsonse = webClient.get()
                    .uri("actuator/mappings")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, new ByteArrayInputStream(gson.toJson(new MappingsResult(repsonse)).getBytes()), "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }

    public EmsResponse clearDatabase() {
        WebClient csrfWebClient = webClientProvider.initCsrfWebClient(entityName);
        try {
            String repsonse = csrfWebClient.delete()
                    .uri("clearDatabase")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, repsonse);
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }
}

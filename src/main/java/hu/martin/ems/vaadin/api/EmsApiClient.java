package hu.martin.ems.vaadin.api;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.internal.LinkedTreeMap;
import hu.martin.ems.core.auth.CustomUserDetailsService;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.vaadin.api.base.WebClientProvider;
import hu.martin.ems.vaadin.core.EmsError;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public abstract class EmsApiClient<T> {

    protected String entityName;
    private Class<T> entityType;

    protected Logger logger;


    @Autowired
    protected WebClientProvider webClientProvider;

    @Autowired
    protected Gson gson;

    @Autowired
    public EmsApiClient(Class<T> entityType) {
        this.entityType = entityType;
        this.entityName = decapitalizeFirstLetter(entityType.getSimpleName());
        this.logger = LoggerFactory.getLogger(entityType);
    }

    private String decapitalizeFirstLetter(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    public EmsResponse save(T entity) {
        CustomUserDetailsService.getLoggedInUsername();
        WebClient webClientCsrf = webClientProvider.initCsrfWebClient(entityName);
        try {
            String response = webClientCsrf.post()
                    .uri("save")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntity(response), "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - save - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class));
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }


    public EmsResponse update(T entity) {
        WebClient webClientCsrf = webClientProvider.initCsrfWebClient(entityName);
        try {
            String response = webClientCsrf.put()
                    .uri("update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntity(response), "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - update - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());

        }
    }

    public EmsResponse restore(T entity) {
        WebClient webClientCsrf = webClientProvider.initCsrfWebClient(entityName);
        try {
            String response = webClientCsrf.put()
                    .uri("restore")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, gson.fromJson(response, entityType), "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - restore - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class));
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }

    public EmsResponse delete(T entity) {
        WebClient webClientCsrf = webClientProvider.initCsrfWebClient(entityName);
        try {
            String json = webClientCsrf.put()
                    .uri("delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, gson.fromJson(json, entityType), "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - delete - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class));
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }


    public EmsResponse permanentlyDelete(Long entityId) {
        WebClient webClientCsrf = webClientProvider.initCsrfWebClient(entityName);
        try {
            String response = webClientCsrf.delete()
                    .uri("permanentlyDelete?id={id}", entityId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - permanently delete - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class));
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }

    public EmsResponse findAll() {
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try {
            String jsonResponse = webClient.mutate().codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                    .build()
                    .get()
                    .uri("findAll")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - findAll - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }

    public EmsResponse findAllWithDeleted() {
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try {
            String jsonResponse = webClient.mutate().codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                    .build()
                    .get()
                    .uri("findAll?withDeleted=true")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - findAllWithDeleted - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }

    public EmsResponse findById(Long id) {
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try {
            String response = webClient.get()
                    .uri("findById?id={id}", id)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, gson.fromJson(response, entityType), "");
        } catch (WebClientResponseException ex) {
            logger.error("WebClient error - findById - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }

    public List<T> convertResponseToEntityList(String jsonResponse) throws JsonParseException {
        return convertResponseToEntityList(jsonResponse, this.entityType);
    }

    public <X> List<X> convertResponseToEntityList(String jsonResponse, Class<X> resultEntityType) throws JsonParseException {
        if (jsonResponse.startsWith("{")) {
            jsonResponse = "[" + jsonResponse + "]";
        }
        List<LinkedTreeMap<String, Object>> mapList = gson.fromJson(jsonResponse, List.class);
        List<X> resultList = new ArrayList<>();
        mapList.forEach(v -> {
            resultList.add(gson.fromJson(gson.toJson(v), resultEntityType));
        });
        return resultList;
    }

    public T convertResponseToEntity(String jsonResponse) {
        return convertResponseToEntityList("[" + jsonResponse + "]").get(0);
    }

    public <X> X convertResponseToEntity(String jsonResponse, Class<X> entityType) {
        return convertResponseToEntityList("[" + jsonResponse + "]", entityType).get(0);
    }

    public String writeValueAsString(T entity) {
        return gson.toJson(entity);
    }
}

package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


@Service
@Slf4j
public abstract class EmsApiClient<T> {
    protected final WebClient webClient;

    private WebClient.Builder webClientBuilder;

    private String entityName;
    private Class<T> entityType;

    protected Logger logger;

    @Autowired
    protected ObjectMapper om;

    public EmsApiClient(Class<T> entityType) {
        this.entityType = entityType;
        this.entityName = decapitalizeFirstLetter(entityType.getSimpleName());
        this.webClientBuilder = WebClient.builder();
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/" + entityName + "/").build();
        this.logger = LoggerFactory.getLogger(entityType);
    }


    private String decapitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    public T save(T entity) {
        try{
            String response =  webClient.post()
                    .uri("save")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return om.readValue(response, new TypeReference<T>() {});
        }
        catch(JsonProcessingException ex){
            logger.error("Saving entity failed due to failing convert it to json. Entity type: " + this.entityName);
            //TODO
            ex.printStackTrace();
            return null;
        }
    }

    public T update(T entity) {
        try{
            String response =  webClient.put()
                    .uri("update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return om.readValue(response, new TypeReference<T>() {});
        }
        catch(JsonProcessingException ex){
            logger.error("Updating entity failed due to failing convert it to json. Entity type: " + this.entityName);
            //TODO
            ex.printStackTrace();
            return null;
        }
    }

    public T restore(T entity){
        try {
            String response = webClient.put()
                    .uri("restore")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return om.readValue(response, new TypeReference<T>() {});
        } catch (JsonProcessingException ex) {
            logger.error("Restoring entity failed due to failing convert it to json. Entity type: " + this.entityName);
            //TODO
            ex.printStackTrace();
            return null;
        }
    }

    public void delete(T entity){
        try {
            webClient.put()
                    .uri("delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (JsonProcessingException ex) {
            logger.error("Saving entity failed due to failing convert it to json. Entity type: " + this.entityName);
            //TODO
            ex.printStackTrace();
        }
    }

    public void permanentlyDelete(Long entityId){
        webClient.delete()
                .uri("permanentlyDelete?id={id}", entityId)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public List<T> findAll(){
        String jsonResponse = webClient.get()
                .uri("findAll")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntityList(jsonResponse);
    }

    protected List<T> convertResponseToEntityList(String jsonResponse){
        return convertResponseToEntityList(jsonResponse, this.entityType);
    }

    protected <X> List<X> convertResponseToEntityList(String jsonResponse, Class<X> resultEntityType) {
        try {
            List<LinkedHashMap<String, Object>> mapList = om.readValue(jsonResponse, new TypeReference<List<LinkedHashMap<String, Object>>>() {});
            List<X> resultList = new ArrayList<>();
            mapList.forEach(v -> {
                resultList.add(om.convertValue(v, resultEntityType));
            });
            return resultList;
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            //TODO
            return null;
        }
    }


    public List<T> findAllWithDeleted(){
        String jsonResponse = webClient.get()
                .uri("findAll?withDeleted=true")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntityList(jsonResponse);
    }

    public T findById(Long id) {
        try{
            String response = webClient.get()
                    .uri("findById?id={id}", id)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return om.readValue(response, new TypeReference<T>(){});
        } catch (JsonProcessingException ex) {
            logger.error("Finding entity failed due to failing convert it from json. Entity type: " + this.entityName);
            ex.printStackTrace();
            return null;
            //TODO
        }
    }

}

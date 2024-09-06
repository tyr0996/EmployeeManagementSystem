package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


@Service
public abstract class EmsApiClient<T> {
    protected final WebClient webClient;

    private WebClient.Builder webClientBuilder;

    private String entityName;
    private Class<T> entityType;

    @Autowired
    private ObjectMapper om;

    public EmsApiClient(Class<T> entityType) {
        this.entityType = entityType;
        this.webClientBuilder = WebClient.builder();
        this.entityName = decapitalizeFirstLetter(entityType.getSimpleName());
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080/api/" + entityName + "/").build();
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
                    .bodyValue(om.writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return om.convertValue(response, entityType);
        }
        catch(JsonProcessingException ex){
            //TODO
            ex.printStackTrace();
            return null;
        }
    }

    public T update(T entity) {
        try{
            String response =  webClient.put()
                    .uri("update")
                    .bodyValue(om.writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return om.convertValue(response, entityType);
        }
        catch(JsonProcessingException ex){
            //TODO
            ex.printStackTrace();
            return null;
        }
    }

    public T restore(T entity){
        try {
            String response = webClient.put()
                    .uri("restore")
                    .bodyValue(om.writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return om.convertValue(response, entityType);
        } catch (JsonProcessingException ex) {
            //TODO
            ex.printStackTrace();
            return null;
        }
    }

    public void delete(T entity){
        try {
            webClient.put()
                    .uri("delete")
                    .bodyValue(om.writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (JsonProcessingException ex) {
            // Handle JSON processing exception
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

    private List<T> convertResponseToEntityList(String jsonResponse){
        try{
            List<LinkedHashMap<String, Object>> mapList = om.readValue(jsonResponse, new TypeReference<List<LinkedHashMap<String, Object>>>() {});
            List<T> resultList = new ArrayList<>();
            mapList.forEach(v -> {
                resultList.add(om.convertValue(v, entityType));
            });
            return resultList;
        }
        catch(JsonProcessingException ex){
            //TODO
            ex.printStackTrace();
            return null;
        }
    }

    public List<T> findAllWithDeleted(){
            String jsonResponse = webClient.get()
                    .uri("findAllWithDeleted")
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
            //TODO
            ex.printStackTrace();
            return null;
        }
    }

}

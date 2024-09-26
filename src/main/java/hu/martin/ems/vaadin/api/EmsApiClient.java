package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


@Service
@Slf4j
@Lazy
public abstract class EmsApiClient<T> {
    protected WebClient webClient;

    private WebClient.Builder webClientBuilder;

    private String entityName;
    private Class<T> entityType;

    protected Logger logger;

    @Autowired
    protected ObjectMapper om;

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    public EmsApiClient(Class<T> entityType) {
        this.entityType = entityType;
        this.entityName = decapitalizeFirstLetter(entityType.getSimpleName());
        this.webClientBuilder = WebClient.builder();
        this.logger = LoggerFactory.getLogger(entityType);
    }

    private String decapitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    public T save(T entity) {
        initWebClient();
        try{
            String response =  webClient.post()
                    .uri("save")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return convertResponseToEntity(response);
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
            return convertResponseToEntity(response);
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
        initWebClient();
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
        initWebClient();
        String response = webClient.delete()
                .uri("permanentlyDelete?id={id}", entityId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public List<T> findAll(){
        initWebClient();
        String jsonResponse = webClient.mutate().codecs(
                    configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                .build()
                .get()
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
        initWebClient();
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

    protected T convertResponseToEntity(String jsonResponse){
        try{
            return convertResponseToEntityList("[" + jsonResponse + "]").get(0);
        }
        catch (Exception e){
            e.printStackTrace();
            //TODO
            return null;
        }
    }


    public List<T> findAllWithDeleted(){
        initWebClient();
        String jsonResponse = webClient.mutate().codecs(
                        configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                .build()
                .get()
                .uri("findAll?withDeleted=true")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntityList(jsonResponse);
    }

    public T findById(Long id) {
        initWebClient();
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

    public void initWebClient(){
        if(webClient == null){
            String baseUrl = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/" + entityName + "/";
            webClient = webClientBuilder.baseUrl(baseUrl).build();
        }
    }
}

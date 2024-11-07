package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.core.config.JacksonConfig;
import hu.martin.ems.core.model.EmsResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Lazy;
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

    protected ObjectMapper om = new JacksonConfig().objectMapper();

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    public EmsApiClient(Class<T> entityType) {
        this.entityType = entityType;
        this.entityName = decapitalizeFirstLetter(entityType.getSimpleName());
        this.webClientBuilder = WebClient.builder();
        this.logger = LoggerFactory.getLogger(entityType);
    }

    private String decapitalizeFirstLetter(String input) {
        return input.substring(0, 1).toLowerCase() + input.substring(1);
    }

    public EmsResponse save(T entity) {
        initWebClient();
        try{
            String response =  webClient.post()
                    .uri("save")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntity(response), "");
        }
        catch(JsonProcessingException ex){
            logger.error("Saving entity failed due to failing convert it to json. Entity type: " + this.entityName);
            return new EmsResponse(500, "Saving " + this.entityName + " failed");
        }

    }

    public EmsResponse update(T entity) {
        initWebClient();
        try{
            String response =  webClient.put()
                    .uri("update")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntity(response), "");
        }
        catch(JsonProcessingException ex){
            logger.error("Updating entity failed due to failing convert it to json. Entity type: " + this.entityName);
            return new EmsResponse(500, "Updating " + this.entityName + " failed");
        }
    }

    public T restore(T entity){
        initWebClient();
        try {
            String response = webClient.put()
                    .uri("restore")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(writeValueAsString(entity))
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
                    .bodyValue(writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (JsonProcessingException ex) {
            logger.error("Saving entity failed due to failing convert it to json. Entity type: " + this.entityName);
            //TODO
            ex.printStackTrace();
        }
    }

    public void clearDatabaseTable(){
        initWebClient();
        webClient.delete()
                .uri("clearDatabaseTable")
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void permanentlyDelete(Long entityId){
        initWebClient();
        String response = webClient.delete()
                .uri("permanentlyDelete?id={id}", entityId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public EmsResponse findAll(){
        initWebClient();
        String jsonResponse = webClient.mutate().codecs(
                    configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                .build()
                .get()
                .uri("findAll")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try{
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException for String: " + jsonResponse);
            return new EmsResponse(500, "JsonProcessingException");
        }
    }

    public List<T> convertResponseToEntityList(String jsonResponse) throws JsonProcessingException {
        return convertResponseToEntityList(jsonResponse, this.entityType);
    }

    public <X> List<X> convertResponseToEntityList(String jsonResponse, Class<X> resultEntityType) throws JsonProcessingException {
        if(jsonResponse.startsWith("{")){
            jsonResponse = "[" + jsonResponse + "]";
        }
        List<LinkedHashMap<String, Object>> mapList = om.readValue(jsonResponse, new TypeReference<List<LinkedHashMap<String, Object>>>() {});
        List<X> resultList = new ArrayList<>();
        mapList.forEach(v -> {
            resultList.add(om.convertValue(v, resultEntityType));
        });
        return resultList;
    }

    public T convertResponseToEntity(String jsonResponse) throws JsonProcessingException {
        return convertResponseToEntityList("[" + jsonResponse + "]").get(0);
    }

    public String writeValueAsString(T entity) throws JsonProcessingException {
        return om.writeValueAsString(entity);
    }


    public EmsResponse findAllWithDeleted() {
        initWebClient();
        String jsonResponse = webClient.mutate().codecs(
                        configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                .build()
                .get()
                .uri("findAll?withDeleted=true")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        } catch (JsonProcessingException e) {
            return new EmsResponse(500, "JsonProcessingException");
        }
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

    public void forcePermanentlyDelete(Long id){
        initWebClient();
        webClient.delete()
                .uri("forcePermanentlyDelete?id={id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }

    public void initWebClient(){
        if(webClient == null){
            String baseUrl = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/" + entityName + "/";
            webClient = webClientBuilder.baseUrl(baseUrl).build();
        }
    }
}

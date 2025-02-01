package hu.martin.ems.vaadin.api;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.internal.LinkedTreeMap;
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
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Service
@Slf4j
//@Lazy
public abstract class EmsApiClient<T> {
    protected WebClient webClient;

    private WebClient.Builder webClientBuilder;

    private String entityName;
    private Class<T> entityType;

    protected Logger logger;


    @Autowired
    @Lazy
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
    protected Gson gson;

    @Autowired
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
            if(response != null && !response.equals("null")){
                return new EmsResponse(200, convertResponseToEntity(response), "");
            }
            return new EmsResponse(500, "Internal server error");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - save - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
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
            if(response != null && !response.equals("null")){
                return new EmsResponse(200, convertResponseToEntity(response), "");
            }
            return new EmsResponse(500, "");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - update - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public T restore(T entity){
        initWebClient();
        try{
            String response = webClient.put()
                    .uri("restore")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return gson.fromJson(response, entityType);
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - restore - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
//            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
            //TODO
            return null;
        }
    }

    public void delete(T entity){
        initWebClient();
        try{
            webClient.put()
                    .uri("delete")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(writeValueAsString(entity))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - delete - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            //TODO
            //return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
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
        try{
            String response = webClient.delete()
                    .uri("permanentlyDelete?id={id}", entityId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - permanently delete - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
//            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
            //TODO
        }
    }

    public EmsResponse findAll(){
        initWebClient();
        try{
            String jsonResponse = webClient.mutate().codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                    .build()
                    .get()
                    .uri("findAll")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(jsonResponse != null && !jsonResponse.equals("null")){
                return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
            }
            return new EmsResponse(500, "Internal Server Error");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - findAll - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public EmsResponse findAllWithNegativeID(){
        initWebClient();
        try{
            String jsonResponse = webClient.mutate().codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                    .build()
                    .get()
                    .uri("findAllWithNegativeID")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(jsonResponse != null && !jsonResponse.equals("null")){
                return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
            }
            return new EmsResponse(500, "Internal Server Error");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - findAllWithNegativeID - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }


    public EmsResponse findAllWithGraph(){
        initWebClient();
        try{
            String jsonResponse = webClient.mutate().codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                    .build()
                    .get()
                    .uri("findAllWithGraph")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(jsonResponse != null && !jsonResponse.equals("null")){
                return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
            }
            return new EmsResponse(500, "Internal Server Error");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - findAllWithGraph - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public EmsResponse findAllWithGraphWithNegativeID(){
        initWebClient();
        try{
            String jsonResponse = webClient.mutate().codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                    .build()
                    .get()
                    .uri("findAllWithGraphWithNegativeID")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(jsonResponse != null && !jsonResponse.equals("null")){
                return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
            }
            return new EmsResponse(500, "Internal Server Error");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - findAllWithGraphWithNegativeID - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }


    public EmsResponse findAllWithGraphWithDeleted(){
        initWebClient();
        try{
            String jsonResponse = webClient.mutate().codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                    .build()
                    .get()
                    .uri("findAllWithGraph?withDeleted=true")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(jsonResponse != null && !jsonResponse.equals("null")){
                return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
            }
            return new EmsResponse(500, "Internal Server Error");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - findAllWithGraphWithDeleted - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public EmsResponse findAllWithGraphWithDeletedWithNegativeID(){
        initWebClient();
        try{
            String jsonResponse = webClient.mutate().codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                    .build()
                    .get()
                    .uri("findAllWithGraphWithNegativeID?withDeleted=true")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(jsonResponse != null && !jsonResponse.equals("null")){
                return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
            }
            return new EmsResponse(500, "Internal Server Error");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - findAllWithGraphWithDeletedWithNegativeID - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public EmsResponse findAllWithDeleted() {
        initWebClient();
        try{
            String jsonResponse = webClient.mutate().codecs(
                            configurer -> configurer.defaultCodecs().maxInMemorySize(16*1024*1024))
                    .build()
                    .get()
                    .uri("findAll?withDeleted=true")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(jsonResponse != null && !jsonResponse.equals("null")){
                return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
            }
            return new EmsResponse(500, "Internal Server Error");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - findAllWithDeleted - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public T findById(Long id) {
        initWebClient();
        String response = webClient.get()
                .uri("findById?id={id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return gson.fromJson(response, entityType);

    }

    public EmsResponse findAllByIds(List<Long> ids){
        initWebClient();
        try{
            String response = webClient.get()
                    .uri("findAllByIds?ids=" + Arrays.toString(ids.toArray()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, gson.fromJson(response, entityType), "");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - findAllByIds - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
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



    public List<T> convertResponseToEntityList(String jsonResponse) throws JsonParseException {
        return convertResponseToEntityList(jsonResponse, this.entityType);
    }

    public <X> List<X> convertResponseToEntityList(String jsonResponse, Class<X> resultEntityType) throws JsonParseException {
        if(jsonResponse == null || jsonResponse.equals("null")){
            throw new JsonParseException("The response was null!");
        }
        if(jsonResponse.startsWith("{")){
            System.out.println(jsonResponse);
            jsonResponse = "[" + jsonResponse + "]";
        }
        List<LinkedTreeMap<String, Object>> mapList = gson.fromJson(jsonResponse, List.class);
        List<X> resultList = new ArrayList<>();
        mapList.forEach(v -> {
            resultList.add(gson.fromJson(gson.toJson(v), resultEntityType));
        });
        return resultList;
    }

    public void initWebClient(){
        if(webClient == null){
            String baseUrl = "http://localhost:" + webServerAppCtxt.getWebServer().getPort() + "/api/" + entityName + "/";
            webClient = webClientBuilder.baseUrl(baseUrl).build();
        }
    }

    public T convertResponseToEntity(String jsonResponse) {
        return convertResponseToEntityList("[" + jsonResponse + "]").get(0);
    }

    public String writeValueAsString(T entity) {
        return gson.toJson(entity);
    }
}

package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Role;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@NeedCleanCoding
public class RoleApiClient extends EmsApiClient<Role> {
    public RoleApiClient() {
        super(Role.class);
    }

    public EmsResponse findByName(String name) {
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try{
            String response = webClient.get()
                    .uri("findByName?name={name}", name)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(response != null && !response.equals("null")){
                return new EmsResponse(200, convertResponseToEntity(response), "");
            }
            return new EmsResponse(500, null, "Internal Server Error");
        }
        catch (
                WebClientResponseException ex){
            logger.error("WebClient error - findByName - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
        }
    }


    public EmsResponse getNoRole(){
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try{
            String response = webClient.get()
                    .uri("getNoRole")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, gson.fromJson(response, Role.class), "");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - findAllByIds - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
        }
    }
}

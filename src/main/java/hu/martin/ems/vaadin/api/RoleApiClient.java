package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.core.EmsError;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@NeedCleanCoding
public class RoleApiClient extends EmsApiClient<Role> {
    public RoleApiClient() {
        super(Role.class);
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
            logger.error("WebClient error - findAllByIds - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(EmsError.class).getError());
        }
    }
}

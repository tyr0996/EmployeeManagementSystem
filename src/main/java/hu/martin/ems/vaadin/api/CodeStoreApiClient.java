package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.CodeStore;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@NeedCleanCoding
public class CodeStoreApiClient extends EmsApiClient<CodeStore> {
    public CodeStoreApiClient() {
        super(CodeStore.class);
    }

    public EmsResponse getChildren(Long parentCodeStoreId){
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try{
            String jsonResponse = webClient.get()
                    .uri("getChildren?parentCodeStoreId=" + parentCodeStoreId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        }
        catch (WebClientResponseException ex){
            logger.error("WebClient error - getChildren - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
        }
    }

    public EmsResponse getAllByName(String name){
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try{
            String jsonResponse = webClient.get()
                    .uri("getByName?name=" + name)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        }
        catch(WebClientResponseException ex) {
            logger.error("WebClient error getAllByName - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
        }
    }
}
package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.CodeStore;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class CodeStoreApiClient extends EmsApiClient<CodeStore> {
    public CodeStoreApiClient() {
        super(CodeStore.class);
    }

    public EmsResponse getChildren(Long parentCodeStoreId){
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("getChildren?parentCodeStoreId=" + parentCodeStoreId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
    }

    public EmsResponse getAllByName(String name){
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("getByName?name=" + name)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
    }
}
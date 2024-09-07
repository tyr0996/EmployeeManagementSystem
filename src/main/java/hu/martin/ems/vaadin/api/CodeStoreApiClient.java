package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.CodeStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CodeStoreApiClient extends EmsApiClient<CodeStore> {
    public CodeStoreApiClient() {
        super(CodeStore.class);
    }

    public List<CodeStore> getChildren(Long parentCodeStoreId){
        String jsonResponse = webClient.get()
                .uri("getChildren?parentCodeStoreId=" + parentCodeStoreId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntityList(jsonResponse);
    }

    public List<CodeStore> getAllByName(String name){
        String jsonResponse = webClient.get()
                .uri("getByName?name=" + name)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntityList(jsonResponse);
    }
}
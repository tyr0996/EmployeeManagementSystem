package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.CodeStore;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CodeStoreApiClient extends EmsApiClient<CodeStore> {
    public CodeStoreApiClient() {
        super(CodeStore.class);
    }

    public List<CodeStore> getChildren(Long parentId){
        //TODO
        return null;
    }

    public List<CodeStore> getAllByName(String name){
        //TODO
        return null;
    }
}

package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Role;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class RoleApiClient extends EmsApiClient<Role> {
    public RoleApiClient() {
        super(Role.class);
    }

    public EmsResponse findByName(String name) {
        initWebClient();
        String response = webClient.get()
                .uri("findByName?name={name}", name)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            return new EmsResponse(200, convertResponseToEntity(response), "");
        } catch (JsonProcessingException e) {
            return new EmsResponse(500, "JsonProcessingException");
        }
    }
}

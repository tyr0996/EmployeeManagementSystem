package hu.martin.ems.vaadin.api;

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
        return new EmsResponse(200, convertResponseToEntity(response), "");
    }
}

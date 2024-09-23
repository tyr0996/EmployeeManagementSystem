package hu.martin.ems.vaadin.api;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.model.Role;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class RoleApiClient extends EmsApiClient<Role> {
    public RoleApiClient() {
        super(Role.class);
    }

    public Role findByName(String name) {
        initWebClient();
        String response = webClient.get()
                .uri("findByName?name={name}", name)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntity(response);
    }
}

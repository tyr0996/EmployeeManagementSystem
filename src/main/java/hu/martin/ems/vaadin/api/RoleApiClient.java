package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleApiClient extends EmsApiClient<Role> {
    public RoleApiClient() {
        super(Role.class);
    }
}

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
}

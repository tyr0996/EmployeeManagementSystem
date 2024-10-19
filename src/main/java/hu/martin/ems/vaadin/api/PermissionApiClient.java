package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.model.Permission;
import org.springframework.stereotype.Component;


@Component
@NeedCleanCoding
public class PermissionApiClient extends EmsApiClient<Permission> {
    public PermissionApiClient() {
        super(Permission.class);
    }
}

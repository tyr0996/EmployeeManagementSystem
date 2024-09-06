package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressApiClient extends EmsApiClient<Address> {
    public AddressApiClient() {
        super(Address.class);
    }
}

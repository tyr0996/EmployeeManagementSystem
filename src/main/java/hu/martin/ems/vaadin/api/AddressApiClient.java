package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.model.Address;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class AddressApiClient extends EmsApiClient<Address> {
    public AddressApiClient() {
        super(Address.class);
    }
}

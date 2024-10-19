package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.model.Customer;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class CustomerApiClient extends EmsApiClient<Customer> {
    public CustomerApiClient() {
        super(Customer.class);
    }
}

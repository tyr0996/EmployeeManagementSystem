package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerApiClient extends EmsApiClient<Customer> {
    public CustomerApiClient() {
        super(Customer.class);
    }
}

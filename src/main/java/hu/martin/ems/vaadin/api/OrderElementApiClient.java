package hu.martin.ems.vaadin.api;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@NeedCleanCoding
public class OrderElementApiClient extends EmsApiClient<OrderElement> {
    public OrderElementApiClient() {
        super(OrderElement.class);
    }

    public List<OrderElement> getByCustomer(Customer customer){
        //TODO
        return null;
    }
}

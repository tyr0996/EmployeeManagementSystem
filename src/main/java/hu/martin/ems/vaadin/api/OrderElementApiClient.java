package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderElementApiClient extends EmsApiClient<OrderElement> {
    public OrderElementApiClient() {
        super(OrderElement.class);
    }

    public List<OrderElement> getByCustomer(Customer customer){
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("getByCustomer?customerId=" + customer.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return convertResponseToEntityList(jsonResponse);
        //TODO valamiért null :(
    }
}

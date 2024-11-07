package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderElementApiClient extends EmsApiClient<OrderElement> {
    public OrderElementApiClient() {
        super(OrderElement.class);
    }

    public EmsResponse getByCustomer(Customer customer){
        initWebClient();
        String jsonResponse = webClient.get()
                .uri("getByCustomer?customerId=" + customer.getId())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try{
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        }
        catch (JsonProcessingException e) {
            return new EmsResponse(500, "JsonProcessingException");
        }
    }
}

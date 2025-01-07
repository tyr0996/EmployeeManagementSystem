package hu.martin.ems.vaadin.api;

import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class OrderElementApiClient extends EmsApiClient<OrderElement> {
    public OrderElementApiClient() {
        super(OrderElement.class);
    }

    public EmsResponse getByCustomer(Customer customer){
        initWebClient();
        try{
            String jsonResponse = webClient.get()
                    .uri("getByCustomer?customerId=" + customer.getId())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        }
        catch (WebClientResponseException ex){
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }
}

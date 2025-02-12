package hu.martin.ems.vaadin.api;

import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class OrderElementApiClient extends EmsApiClient<OrderElement> {
    public OrderElementApiClient() {
        super(OrderElement.class);
    }

    public EmsResponse getByCustomer(Customer customer){
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try{
            String jsonResponse = webClient.get()
                    .uri("getByCustomer?customerId=" + customer.getId())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(jsonResponse != null && !jsonResponse.equals("null")){
                return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
            }
            return new EmsResponse(500, "Internal server error");
        }
        catch (WebClientResponseException ex){
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
        }
    }
}

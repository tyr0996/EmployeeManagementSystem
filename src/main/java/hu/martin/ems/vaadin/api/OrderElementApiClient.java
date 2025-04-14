package hu.martin.ems.vaadin.api;

import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Customer;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.model.Supplier;
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
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        }
        catch (WebClientResponseException ex){
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
        }
    }

    public EmsResponse getBySupplier(Supplier supplier){
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try{
            String jsonResponse = webClient.get()
                    .uri("getBySupplier?supplierId=" + supplier.getId())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntityList(jsonResponse), "");
        }
        catch (WebClientResponseException ex){
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAs(Error.class).getError());
        }
    }
}

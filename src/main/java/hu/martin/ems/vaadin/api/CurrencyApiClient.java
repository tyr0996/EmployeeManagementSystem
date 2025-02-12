package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Currency;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;

@Component
@NeedCleanCoding
public class CurrencyApiClient extends EmsApiClient<Currency>{
    public CurrencyApiClient() {
        super(Currency.class);
    }

    public EmsResponse fetchAndSaveRates(){
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try{
            String response = webClient.get()
                    .uri("fetchAndSaveRates")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if(response != null && !response.equals("null")){
                return new EmsResponse(200, response, "");
            }
            return new EmsResponse(500, "Internal server error");
        }
        catch(WebClientResponseException ex){
            System.out.println(ex.getResponseBodyAsString());
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public EmsResponse findByDate(LocalDate date) {
        WebClient webClient = webClientProvider.initWebClient(entityName);
        String response = webClient.get()
                .uri("findByDate?date={year}-{month}-{day}", date.getYear(), date.getMonthValue(), date.getDayOfMonth())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        if(response != null && response instanceof String && !response.equals("null")){
            return new EmsResponse(200, gson.fromJson((String) response, Currency.class), "");
        }
        else if(response instanceof String && response.equals("null")){
            return new EmsResponse(200, null);
        }
        else{
            return new EmsResponse(500, "Error happened while getting currencies by wdate: Internal server error");
        }
    }
}

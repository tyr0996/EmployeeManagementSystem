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
            return new EmsResponse(200, response, "");
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), gson.fromJson(ex.getResponseBodyAsString(), Error.class).getError());
        }
    }

    public EmsResponse findByDate(LocalDate date) {
        WebClient webClient = webClientProvider.initWebClient(entityName);
        try{
            String response = webClient.get()
                    .uri("findByDate?date={date}", date)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, convertResponseToEntity(response, Currency.class), "");
        }
        catch (WebClientResponseException ex){
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }

    }
}

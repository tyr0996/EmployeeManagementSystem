package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Currency;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;

@Component
@NeedCleanCoding
public class CurrencyApi extends EmsApiClient<Currency>{
    public CurrencyApi() {
        super(Currency.class);
    }

    public EmsResponse fetchAndSaveRates(){
        initWebClient();
        try{
            String response = webClient.get()
                    .uri("fetchAndSaveRates")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return gson.fromJson(response, EmsResponse.class);
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public EmsResponse findByDate(LocalDate date) {
        initWebClient();
        String response = webClient.get()
                .uri("findByDate?date={year}-{month}-{day}", date.getYear(), date.getMonthValue(), date.getDayOfMonth())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return new EmsResponse(200, gson.fromJson(response, Currency.class), "");

    }
}

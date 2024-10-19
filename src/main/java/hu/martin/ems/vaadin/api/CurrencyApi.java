package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
            return om.readValue(response, new TypeReference<EmsResponse>() {});
        }
        catch (JsonProcessingException ex) {
            logger.error("Finding entity failed due to failing convert it from json. Entity type: Currency");
            ex.printStackTrace();
            return null;
            //TODO
        }
        catch(WebClientResponseException ex){
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public Currency findByDate(LocalDate date) {
        initWebClient();
        try{
            String response = webClient.get()
                    .uri("findByDate?date={year}-{month}-{day}", date.getYear(), date.getMonthValue(), date.getDayOfMonth())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return om.readValue(response, new TypeReference<Currency>(){});
        } catch (JsonProcessingException ex) {
            logger.error("Finding entity failed due to failing convert it from json. Entity type: Currency");
            ex.printStackTrace();
            return null;
            //TODO
        }
    }
}

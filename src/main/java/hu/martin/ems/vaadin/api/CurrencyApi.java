package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.model.Currency;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@NeedCleanCoding
public class CurrencyApi extends EmsApiClient<Currency>{
    public CurrencyApi() {
        super(Currency.class);
    }

    public Currency fetchAndSaveRates(){
        initWebClient();
        try{
            String response = webClient.get()
                    .uri("fetchAndSaveRates")
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

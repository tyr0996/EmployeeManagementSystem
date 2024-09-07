package hu.martin.ems.vaadin.api;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.model.Currency;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@NeedCleanCoding
public class CurrencyApi {
    public Currency fetchAndSaveRates(){
        return null;
        //TODO
    }

    public Currency findByDate(LocalDate date) {
        return null;
        //TODO
    }
}

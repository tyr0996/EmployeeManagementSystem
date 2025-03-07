package hu.martin.ems.core.schedule;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.exception.CurrencyException;
import hu.martin.ems.service.CurrencyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class CurrencyScheduler {

    private final CurrencyService currencyService;

    public CurrencyScheduler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Scheduled(fixedRate = 3600000)
    public void fetchRates() {
        try{
            currencyService.fetchAndSaveRates();
        }
        catch (CurrencyException e){
            //TODO megírni a kivételkezelést
        }
    }
}
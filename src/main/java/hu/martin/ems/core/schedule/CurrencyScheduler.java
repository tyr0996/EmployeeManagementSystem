package hu.martin.ems.core.schedule;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.exception.CurrencyException;
import hu.martin.ems.service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class CurrencyScheduler {

    private final CurrencyService currencyService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public CurrencyScheduler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Scheduled(cron = "${schedule.fetch.currencies.cron}")
    public void fetchRates() {
        try{
            currencyService.fetchAndSaveRates();
        }
        catch (CurrencyException e){
            logger.error("Scheduled currency fetching method failed: " + e.getType().getText() + ". It needs to fetch manually.");
        }
    }
}
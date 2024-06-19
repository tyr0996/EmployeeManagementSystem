package hu.martin.ems.core.schedule;

import hu.martin.ems.service.CurrencyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CurrencyScheduler {

    private final CurrencyService currencyService;

    public CurrencyScheduler(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @Scheduled(fixedRate = 3600000)
    public void fetchRates() {
        currencyService.fetchAndSaveRates();
    }
}
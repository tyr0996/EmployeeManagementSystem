package hu.martin.ems;

import hu.martin.ems.schedule.CurrencyScheduler;
import org.awaitility.Awaitility;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScheduledTests extends BaseCrudTest {

    @Test
    void scheduledCurrencyFetchingTest() {
        dp.executeSQL("DELETE FROM Currency");
        assertNull(currencyRepository.findByDate(LocalDate.now()));
        context.getBean(CurrencyScheduler.class).fetchRates();

        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(spyCurrencyScheduler, times(1)).fetchRates();
                    verify(spyCurrencyService, times(1)).fetchAndSaveRates();
                    assertNotNull(currencyRepository.findByDate(LocalDate.now()));
                });
    }

    @Test
    void scheduledCurrencyFetchingFailedTest() {
        String fetchingCurrencyApiUrl = environment.getProperty("api.currency.url");
        String baseCurrency = environment.getProperty("api.currency.baseCurrency");

        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error")).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));
        dp.executeSQL("DELETE FROM Currency");
        assertNull(currencyRepository.findByDate(LocalDate.now()));
        Mockito.clearInvocations(spyCurrencyScheduler);
        Mockito.clearInvocations(spyCurrencyService);
        context.getBean(CurrencyScheduler.class).fetchRates();
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(spyCurrencyScheduler, times(1)).fetchRates();
                    verify(spyCurrencyService, times(1)).fetchAndSaveRates();
                    verify(spyRestTemplate, times(1)).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));
                });
        assertNull(currencyRepository.findByDate(LocalDate.now()));
    }
}

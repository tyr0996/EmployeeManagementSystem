package hu.martin.ems;

import hu.martin.ems.repository.CurrencyRepository;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "schedule.fetch.currencies.cron=*/1 * * * * *"
})
public class ScheduledTests extends BaseCrudTest {


    @Autowired
    protected CurrencyRepository currencyRepo;

    @Autowired
    private Environment env;

    @Test
    void scheduledCurrencyFetchingTest() {
        dataProvider.executeSQL("DELETE FROM Currency");
        assertNull(currencyRepo.findByDate(LocalDate.now()));
        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(spyCurrencyScheduler, times(1)).fetchRates();
                    verify(spyCurrencyService, times(1)).fetchAndSaveRates();
                    assertNotNull(currencyRepo.findByDate(LocalDate.now()));
                });
    }

    @Test
    void scheduledCurrencyFetchingFailedTest() {
        fetchingCurrencyApiUrl = env.getProperty("api.currency.url");
        baseCurrency = env.getProperty("api.currency.baseCurrency");

        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error")).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));
        dataProvider.executeSQL("DELETE FROM Currency");
        assertNull(currencyRepo.findByDate(LocalDate.now()));
        Awaitility.await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    verify(spyCurrencyScheduler, times(1)).fetchRates();
                    verify(spyCurrencyService, times(1)).fetchAndSaveRates();
                    verify(spyRestTemplate, times(1)).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));
                    assertNull(currencyRepo.findByDate(LocalDate.now()));
                });
    }
}

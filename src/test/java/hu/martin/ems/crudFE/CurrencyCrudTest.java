package hu.martin.ems.crudFE;

import com.fasterxml.jackson.annotation.JsonProperty;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.controller.CurrencyController;
import hu.martin.ems.core.apiresponse.CurrencyAPI;
import hu.martin.ems.core.apiresponse.CurrencyResponse;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.date.Date;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.model.Currency;
import hu.martin.ems.repository.CodeStoreRepository;
import hu.martin.ems.repository.CurrencyRepository;
import hu.martin.ems.service.CodeStoreService;
import hu.martin.ems.service.CurrencyService;
import org.checkerframework.checker.units.qual.A;
import org.mockito.*;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CurrencyCrudTest extends BaseCrudTest {

    private static final String datePickerXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-date-picker/input";

    private static final String gridXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";

    private static final String fetchButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-button";
    private static CrudTestingUtil crudTestingUtil;


    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Product", null, gridXPath, null);
        GridTestingUtil.driver = driver;
    }

    @Test
    public void selectDateRetroactively_NotSavedDate() throws InterruptedException {
        spyCurrencyRepository.customClearDatabaseTable();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);
        LocalDate today = LocalDate.now();
        String todayString = today.format(DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        assertEquals(todayString, findVisibleElementWithXpath(datePickerXPath).getAttribute("value"));
        Thread.sleep(1000);
        assertEquals(162, countVisibleGridDataRows(gridXPath));
        WebElement datePicker = findVisibleElementWithXpath(datePickerXPath);
        datePicker.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        datePicker.sendKeys(RandomGenerator.generateRandomDate());
        datePicker.sendKeys(Keys.ENTER);
        Thread.sleep(100);
        checkNotificationText("Exchange rates cannot be downloaded retroactively!");
        assertEquals(todayString, findVisibleElementWithXpath(datePickerXPath).getAttribute("value"));
    }

    //@Test
    public void tryToEnterAllPossibleGoodDateFormats() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);
        LocalDate today = LocalDate.now();
        String todayString = today.format(DateTimeFormatter.ofPattern("yyyy. MM. dd"));
        assertEquals(todayString, findVisibleElementWithXpath(datePickerXPath).getAttribute("value"));
        Thread.sleep(1000);
        assertEquals(162, countVisibleGridDataRows(gridXPath));
        WebElement datePicker = findVisibleElementWithXpath(datePickerXPath);
        List<String> generatedDates = Date.generateAllFormatDate(today);
        for(String generatedTodayDate : generatedDates){
            datePicker.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
            datePicker.sendKeys(generatedTodayDate);
            datePicker.sendKeys(Keys.ENTER);
            assertEquals(todayString, datePicker.getAttribute("value"));
        }
    }

    @Test
    public void checkEuroExistsInGrid() throws InterruptedException {
        clearCurrencyDatabaseTable();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);
        Thread.sleep(100);
        assertEquals(1, countElementResultsFromGridWithFilter(gridXPath,  "EUR", ""));
    }

    @Test
    public void checkEuroValueExistsInGrid() throws InterruptedException {
        clearCurrencyDatabaseTable();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);
        Thread.sleep(100);
        applyFilter(gridXPath, "EUR");
        String[] eurData = getDataFromRowLocation(gridXPath, new ElementLocation(1, 0), false);
        resetFilter(gridXPath);
        assertEquals(1, countElementResultsFromGridWithFilter(gridXPath,  "", eurData[1]));
    }

    @Test
    public void fetchingCurrenciesFailedThenSuccessTest() throws InterruptedException {
        clearCurrencyDatabaseTable();
        Object originalCurrency = BeanProvider.getBean(RestTemplate.class).getForObject(fetchingCurrencyApiUrl + baseCurrency, Object.class);

        Mockito.doThrow(new RestClientException("")).doReturn(originalCurrency).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));
        Mockito.doReturn(null).when(spyCurrencyRepository).findByDate(Mockito.any(LocalDate.class));

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);
        checkNotificationText(EmsResponse.Description.FETCHING_CURRENCIES_FAILED);
        assertEquals(0, countVisibleGridDataRows(gridXPath));

        Mockito.reset(spyCurrencyRepository);

        findVisibleElementWithXpath(fetchButtonXpath).click();
        //checkNotificationText("Fetching exchange rates was successful!");

        findVisibleElementWithXpath(fetchButtonXpath).click();
        //checkNotificationText("Currencies already fetched");
    }

    @Test
    public void fetchingCurrenciesSuccessTest() throws InterruptedException {
        clearCurrencyDatabaseTable();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);
        checkNotificationText("Fetching exchange rates was successful!");
        assertEquals(true, countVisibleGridDataRows(gridXPath) > 0);

        findVisibleElementWithXpath(fetchButtonXpath).click();
        Thread.sleep(100);
        checkNotificationText("Currencies already fetched");
        assertEquals(true, countVisibleGridDataRows(gridXPath) > 0);
    }

    @Test
    public void fetchingCurrenciesNotCorrectResponseTest() {
        clearCurrencyDatabaseTable();
        /*
        TODO Valamiért így kell megcsinálni, mert ha a @Mock-okat használom, akkor az ezutáni tesztek,
         amik ezeket a mockolt dolgokat használják, meghalnak, mert hiába hívtam meg a Mockito.reset(repo, service)-t ugyan úgy
         a mockolásokat használja továbbra is. A MockedStatic-kal csinálom, akkor nem megy bele az exception-ba, mert ugyan
         a Mockito úgy érzékeli, hogy az egy static függvény (interface miatt), pedig nem az a @Query miatt (és ráadásul nem
         is tudom úgy meghívni, hogy CurrencyRepository.findByDate(LocalDate)). Így most ez a megoldás, de később javítani kell.
         */

        LinkedHashMap<String, Object> badResponse = new LinkedHashMap<>();
        badResponse.put("provider", "not used in my code");
        badResponse.put("WARNING_UPGRADE_TO_V6", "not used in my code");
        badResponse.put("terms", "not used in my code");
        badResponse.put("base", "HUF");
        badResponse.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        badResponse.put("time_last_updated", 135441L);
        badResponse.put("rates", "{AED:Ez a nemjó,AFN:5.495,ALL:4.049,AMD:0.952,ANG:205.761,AOA:0.395,ARS:0.376,AUD:246.305,AWG:205.761,AZN:216.45,BAM:204.918,BBD:184.162,BDT:3.086,BGN:204.918,BHD:980.392,BIF:0.126,BMD:369.004,BND:280.899,BOB:52.91,BRL:65.359,BSD:369.004,BTN:4.386,BWP:27.624,BYN:112.613,BZD:184.162,CAD:267.38,CDF:0.13,CHF:427.35,CLP:0.391,CNY:51.813,COP:0.087,CRC:0.714,CUP:15.361,CVE:3.636,CZK:15.848,DJF:2.075,DKK:53.763,DOP:6.098,DZD:2.755,EGP:7.576,ERN:24.57,ETB:3.067,EUR:401.606,FJD:164.745,FKP:480.769,FOK:53.763,GBP:480.769,GEL:135.135,GGP:480.769,GHS:22.936,GIP:480.769,GMD:5.208,GNF:0.042,GTQ:47.619,GYD:1.757,HKD:47.393,HNL:14.684,HRK:53.191,HTG:2.786,HUF:1.0,IDR:0.024,ILS:98.039,IMP:480.769,INR:4.386,IQD:0.281,IRR:0.009,ISK:2.681,JEP:480.769,JMD:2.326,JOD:520.833,JPY:2.469,KES:2.849,KGS:4.31,KHR:0.09,KID:246.305,KMF:0.813,KRW:0.27,KWD:1201.923,KYD:442.478,KZT:0.758,LAK:0.017,LBP:0.004,LKR:1.255,LRD:1.908,LSL:20.921,LYD:76.336,MAD:37.453,MDL:20.833,MGA:0.08,MKD:6.536,MMK:0.128,MNT:0.108,MOP:46.083,MRU:9.259,MUR:7.937,MVR:23.81,MWK:0.211,MXN:18.587,MYR:86.207,MZN:5.747,NAD:20.921,NGN:0.225,NIO:10.0,NOK:33.898,NPR:2.74,NZD:223.714,OMR:961.538,PAB:369.004,PEN:98.039,PGK:93.458,PHP:6.369,PKR:1.328,PLN:93.458,PYG:0.047,QAR:101.215,RON:80.645,RSD:3.425,RUB:3.817,RWF:0.265,SAR:98.039,SBD:45.455,SCR:25.445,SDG:0.82,SEK:35.211,SGD:280.899,SHP:480.769,SLE:15.898,SLL:0.016,SOS:0.641,SRD:11.211,SSP:0.115,STN:16.367,SYP:0.029,SZL:20.921,THB:11.111,TJS:34.483,TMT:105.152,TND:119.474,TOP:161.551,TRY:10.764,TTD:54.348,TVD:246.305,TWD:11.416,TZS:0.135,UAH:8.929,UGX:0.1,USD:369.004,UYU:8.85,UZS:0.029,VES:9.434,VND:0.015,VUV:3.185,WST:138.122,XAF:0.61,XCD:136.426,XDR:490.196,XOF:0.61,XPF:3.356,YER:1.468,ZAR:20.921,ZMW:13.793,ZWL:13.812}");

        Mockito.doReturn(badResponse).doReturn(badResponse).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));
        Mockito.doReturn(null).when(spyCurrencyRepository).findByDate(Mockito.any(LocalDate.class));

//        BeanProvider.getBean(CurrencyController.class).setService(injectedCurrencyService);

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CURRENCY_SUBMENU);
        findVisibleElementWithXpath(gridXPath);
        checkNotificationText("Currencies fetched successfully, but the currency server sent bad data");
        //checkNotificationText(EmsResponse.Description.PARSING_CURRENCIES_FAILED);

        findVisibleElementWithXpath(fetchButtonXpath).click();
        checkNotificationText("Currencies fetched successfully, but the currency server sent bad data");

//        BeanProvider.getBean(CurrencyController.class).setService(originalService);
    }

    private void clearCurrencyDatabaseTable(){
        List<Currency> currentCurrencies = spyCurrencyRepository.customFindAll(false);
        currentCurrencies.forEach(v -> {
            spyCurrencyRepository.customForcePermanentlyDelete(v.getId());
        });
    }
}

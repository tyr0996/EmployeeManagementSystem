package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.date.DateUtil;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.pages.CurrencyPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
public class CurrencyCrudTest extends BaseCrudTest {
    @Test
    @Video
    public void selectDateRetroactively_NotSavedDate() {
        clearCurrencyDatabaseTable();

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);
        CurrencyPage page = new CurrencyPage(driver, port);
        assertEquals(LocalDate.now(), page.getDatePicker().getDate());

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Fetching exchange rates was successful!", notification.getText());
        notification.close();

        assertEquals(163, page.getGrid().getPaginationData().getTotalElements().intValue());
        page.getDatePicker().selectDate(RandomGenerator.generateRandomDate());

        VaadinNotificationComponent notificationRetro = new VaadinNotificationComponent(driver);
        assertEquals("Exchange rates cannot be downloaded retroactively!", notificationRetro.getText());
        notificationRetro.close();

        assertEquals(LocalDate.now(), page.getDatePicker().getDate());

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    @Video
    public void tryToEnterAllPossibleGoodDateFormats() {
        clearCurrencyDatabaseTable();
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);
        CurrencyPage page = new CurrencyPage(driver, port);
        assertEquals(LocalDate.now(), page.getDatePicker().getDate());

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Fetching exchange rates was successful!", notification.getText());
        notification.close();

        LocalDate todayDate = LocalDate.now();

        List<String> generatedDates = DateUtil.generateAllFormatDate(todayDate);
        Collections.shuffle(generatedDates);
        List<String> shorted = generatedDates.subList(0, 30);
        for(String generatedTodayDate : shorted){
            page.getDatePicker().clear();
            page.getDatePicker().selectDate(generatedTodayDate);
            assertEquals(todayDate, page.getDatePicker().getDate());
        }

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }



    @Test
    public void checkEuroExistsInGrid() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);
        CurrencyPage page = new CurrencyPage(driver, port);
        page.getGrid().applyFilter("EUR", "");
        assertEquals(1, page.getGrid().getPaginationData().getTotalElements().intValue());
        page.getGrid().resetFilter();
        assertEquals(163, page.getGrid().getPaginationData().getTotalElements().intValue());

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void checkEuroValueExistsInGrid() {
        clearCurrencyDatabaseTable();
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Fetching exchange rates was successful!");
        notification.close();

        CurrencyPage page = new CurrencyPage(driver, port);
        page.getGrid().applyFilter("EUR", "");

        String[] eurData = page.getGrid().getDataFromRowLocation(new ElementLocation(1, 0), false);
        page.getGrid().resetFilter();
        page.getGrid().applyFilter(eurData);
        assertEquals(1, page.getGrid().getPaginationData().getTotalElements().intValue());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    @Video
    public void fetchingCurrenciesFailedThenSuccessTest() {
        clearCurrencyDatabaseTable();
        Object originalCurrency = BeanProvider.getBean(RestTemplate.class).getForObject(fetchingCurrencyApiUrl + baseCurrency, Object.class);

        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error")).doReturn(originalCurrency).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);
        CurrencyPage page = new CurrencyPage(driver, port);

        VaadinNotificationComponent notificationFailed = new VaadinNotificationComponent(driver);
        assertEquals(EmsResponse.Description.FETCHING_CURRENCIES_FAILED, notificationFailed.getText());
        notificationFailed.close();
        assertEquals(0, page.getGrid().getPaginationData().getTotalElements().intValue());

        page.getFetchButton().click();
        VaadinNotificationComponent notificationSucc = new VaadinNotificationComponent(driver);
        assertEquals("Fetching exchange rates was successful!", notificationSucc.getText());
        notificationSucc.close();
        assertEquals(163, page.getGrid().getPaginationData().getTotalElements().intValue());
        page.getFetchButton().click();

        VaadinNotificationComponent notificationAlready = new VaadinNotificationComponent(driver);
        assertEquals("Currencies already fetched", notificationAlready.getText());
        notificationAlready.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    @Video
    public void fetchingCurrenciesSuccessTest() {
        clearCurrencyDatabaseTable();

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);
        CurrencyPage page = new CurrencyPage(driver, port);
        assertEquals(LocalDate.now(), page.getDatePicker().getDate());

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Fetching exchange rates was successful!", notification.getText());
        notification.close();
        assertEquals(163, page.getGrid().getPaginationData().getTotalElements().intValue());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    @Video
    public void checkRetroCurrenciesTest() throws IOException {
        clearCurrencyDatabaseTable();
        dp.executeSQLFile(new File(dp.getGENERATED_SQL_FILES_PATH() + "\\currencies.sql"));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);
        CurrencyPage page = new CurrencyPage(driver, port);
        assertEquals(LocalDate.now(), page.getDatePicker().getDate());

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Fetching exchange rates was successful!", notification.getText());
        notification.close();
        assertEquals(163, page.getGrid().getPaginationData().getTotalElements().intValue());
        page.getGrid().applyFilter("EUR", "");
        page.getGrid().waitForRefresh();
        String[] todayEurExchangeRate = page.getGrid().getDataFromRowLocation(new ElementLocation(1, 0), false);
        page.getGrid().resetFilter();

        page.getDatePicker().selectDate(LocalDate.of(2025, 5, 11));
        page.getGrid().waitForRefresh();
        page.getGrid().applyFilter("EUR", "");
        page.getGrid().waitForRefresh();
        assertEquals(LocalDate.of(2025, 5, 11), page.getDatePicker().getDate());
        String[] oldEurExchangeRate = page.getGrid().getDataFromRowLocation(new ElementLocation(1, 0), false);
        page.getGrid().resetFilter();

        assertEquals("EUR", todayEurExchangeRate[0]);
        assertEquals("EUR", oldEurExchangeRate[0]);
        assertFalse(oldEurExchangeRate[1].equals(todayEurExchangeRate[1]));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    @Video
    public void fetchingCurrenciesNotCorrectResponseTest() {
        clearCurrencyDatabaseTable();
        LinkedHashMap<String, Object> badResponse = new LinkedHashMap<>();
        badResponse.put("provider", "not used in my code");
        badResponse.put("WARNING_UPGRADE_TO_V6", "not used in my code");
        badResponse.put("terms", "not used in my code");
        badResponse.put("base", "HUF");
        badResponse.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        badResponse.put("time_last_updated", 135441L);
        badResponse.put("rates", "{AED:\"Ez a nemjó\",AFN:5.495,ALL:4.049,AMD:0.952,ANG:205.761,AOA:0.395,ARS:0.376,AUD:246.305,AWG:205.761,AZN:216.45,BAM:204.918,BBD:184.162,BDT:3.086,BGN:204.918,BHD:980.392,BIF:0.126,BMD:369.004,BND:280.899,BOB:52.91,BRL:65.359,BSD:369.004,BTN:4.386,BWP:27.624,BYN:112.613,BZD:184.162,CAD:267.38,CDF:0.13,CHF:427.35,CLP:0.391,CNY:51.813,COP:0.087,CRC:0.714,CUP:15.361,CVE:3.636,CZK:15.848,DJF:2.075,DKK:53.763,DOP:6.098,DZD:2.755,EGP:7.576,ERN:24.57,ETB:3.067,EUR:401.606,FJD:164.745,FKP:480.769,FOK:53.763,GBP:480.769,GEL:135.135,GGP:480.769,GHS:22.936,GIP:480.769,GMD:5.208,GNF:0.042,GTQ:47.619,GYD:1.757,HKD:47.393,HNL:14.684,HRK:53.191,HTG:2.786,HUF:1.0,IDR:0.024,ILS:98.039,IMP:480.769,INR:4.386,IQD:0.281,IRR:0.009,ISK:2.681,JEP:480.769,JMD:2.326,JOD:520.833,JPY:2.469,KES:2.849,KGS:4.31,KHR:0.09,KID:246.305,KMF:0.813,KRW:0.27,KWD:1201.923,KYD:442.478,KZT:0.758,LAK:0.017,LBP:0.004,LKR:1.255,LRD:1.908,LSL:20.921,LYD:76.336,MAD:37.453,MDL:20.833,MGA:0.08,MKD:6.536,MMK:0.128,MNT:0.108,MOP:46.083,MRU:9.259,MUR:7.937,MVR:23.81,MWK:0.211,MXN:18.587,MYR:86.207,MZN:5.747,NAD:20.921,NGN:0.225,NIO:10.0,NOK:33.898,NPR:2.74,NZD:223.714,OMR:961.538,PAB:369.004,PEN:98.039,PGK:93.458,PHP:6.369,PKR:1.328,PLN:93.458,PYG:0.047,QAR:101.215,RON:80.645,RSD:3.425,RUB:3.817,RWF:0.265,SAR:98.039,SBD:45.455,SCR:25.445,SDG:0.82,SEK:35.211,SGD:280.899,SHP:480.769,SLE:15.898,SLL:0.016,SOS:0.641,SRD:11.211,SSP:0.115,STN:16.367,SYP:0.029,SZL:20.921,THB:11.111,TJS:34.483,TMT:105.152,TND:119.474,TOP:161.551,TRY:10.764,TTD:54.348,TVD:246.305,TWD:11.416,TZS:0.135,UAH:8.929,UGX:0.1,USD:369.004,UYU:8.85,UZS:0.029,VES:9.434,VND:0.015,VUV:3.185,WST:138.122,XAF:0.61,XCD:136.426,XDR:490.196,XOF:0.61,XPF:3.356,YER:1.468,ZAR:20.921,ZMW:13.793,ZWL:13.812}");

        Mockito.doReturn(badResponse).doReturn(badResponse).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));
//        Mockito.doReturn(null).when(spyCurrencyService).findByDate(Mockito.any(LocalDate.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);
        CurrencyPage page = new CurrencyPage(driver, port);
        assertEquals(LocalDate.now(), page.getDatePicker().getDate());

        VaadinNotificationComponent notificationFailed1 = new VaadinNotificationComponent(driver);
        assertEquals("Currencies fetched successfully, but the currency server sent bad data", notificationFailed1.getText());
        notificationFailed1.close();
        assertEquals(0, page.getGrid().getPaginationData().getTotalElements().intValue());

        page.getFetchButton().click();
        VaadinNotificationComponent notificationFailed2 = new VaadinNotificationComponent(driver);
        assertEquals("Currencies fetched successfully, but the currency server sent bad data", notificationFailed2.getText());
        notificationFailed2.close();
        assertEquals(0, page.getGrid().getPaginationData().getTotalElements().intValue());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    @Video
    public void nullResponseWhenFetchAndSaveRates() throws SQLException {
        clearCurrencyDatabaseTable();
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(4));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);
        CurrencyPage page = new CurrencyPage(driver, port);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Database error", notification.getText());
        notification.close();
        assertEquals(0, page.getGrid().getPaginationData().getTotalElements().intValue());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    @Test
    @Video
    public void databaseNotAvailableWhileFindCurrencyByDate() throws SQLException {
        clearCurrencyDatabaseTable();
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 2);
//        Mockito.doThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Database error")).when(spyRestTemplate).getForObject(Mockito.eq(fetchingCurrencyApiUrl + baseCurrency), Mockito.any(Class.class));

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CURRENCY_SUBMENU);
        CurrencyPage page = new CurrencyPage(driver, port);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("EmsError happened while getting currencies by date", notification.getText());
        notification.close();
        assertEquals(0, page.getGrid().getPaginationData().getTotalElements().intValue());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


}

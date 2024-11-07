package hu.martin.ems;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.config.JacksonConfig;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.CurrencyRepository;
import hu.martin.ems.service.CurrencyService;
import hu.martin.ems.vaadin.api.*;
import hu.martin.ems.vaadin.component.ComponentManager;
import org.checkerframework.checker.units.qual.A;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.*;

import java.util.HashMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BaseCrudTest extends AbstractTestNGSpringContextTests {

    //TODO megcsinálni, hogy ha módosítottunk egy elemet vagy valamit, akkor ellenőrizzük a létrehozás gombot! fontos, hogy üres legyen a form és létrehozás legyen a címben!

    protected static WebDriver driver;

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
    protected DataProvider dataProvider;

    @Autowired
    private Environment env;

    protected static Integer port;
    protected static DataProvider dp;

    public static String downloadPath;

    @Autowired
    @SpyBean
    protected static CurrencyRepository spyCurrencyRepository;

    @Autowired
    @SpyBean
    protected static RestTemplate spyRestTemplate;

    @Autowired
    @SpyBean
    protected static CurrencyService injectedCurrencyService;

    @SpyBean
    @Autowired
    protected static ComponentManager spyComponentManager;

    @SpyBean
    @Autowired
    protected static EmployeeApiClient spyEmployeeApiClient;

    @SpyBean
    @Autowired
    protected static AddressApiClient spyAddressApiClient;

    @SpyBean
    @Autowired
    protected static AdminToolsApiClient spyAdminToolsApiClientClient;

    @SpyBean
    @Autowired
    protected static CityApiClient spyCityApiClient;

    @SpyBean
    @Autowired
    protected static CodeStoreApiClient spyCodeStoreApiClient;

    @SpyBean
    @Autowired
    protected static CurrencyApi spyCurrencyApiClient; // TODO rename to CurrencyApiClient the class

    @SpyBean
    @Autowired
    protected static CustomerApiClient spyCustomerApiClient;

    @SpyBean
    @Autowired
    protected static EmailSendingApi spyEmailSendingApi;

    @SpyBean
    @Autowired
    protected static OrderApiClient spyOrderApiClient;

    @SpyBean
    @Autowired
    protected static OrderElementApiClient spyOrderElementApiClient;

    @SpyBean
    @Autowired
    protected static PermissionApiClient spyPermissionApiClient;

    @SpyBean
    @Autowired
    protected static ProductApiClient spyProductApiClient;

    @SpyBean
    @Autowired
    protected static RoleApiClient spyRoleApiClient;

    @SpyBean
    @Autowired
    protected static RoleXPermissionApiClient spyRoleXPermissionApiClient;

    @SpyBean
    @Autowired
    protected static SupplierApiClient spySupplierApiClient;

    @SpyBean
    @Autowired
    protected static UserApiClient spyUserApiClient;

    @SpyBean
    @Autowired
    protected static ObjectMapper spyObjectMapper;


    protected static String fetchingCurrencyApiUrl;

    protected static String baseCurrency;


    @BeforeSuite(alwaysRun = true)
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();

        downloadPath = env.getProperty("chrome.download.path");
        fetchingCurrencyApiUrl = env.getProperty("api.currency.url");
        baseCurrency = env.getProperty("api.currency.baseCurrency");

        ChromeOptions options = new ChromeOptions();
        HashMap<String, Object> chromePref = new HashMap<>();

        chromePref.put("download.default_directory", downloadPath);
        chromePref.put("download.prompt_for_download", false);
        chromePref.put("directory_upgrade", true);
        options.setExperimentalOption("prefs", chromePref);
        //options.addArguments("--headless");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        port = webServerAppCtxt.getWebServer().getPort();
        dp = dataProvider;
    }

    @AfterMethod(alwaysRun = true)
    @BeforeMethod(alwaysRun = true)
    public void afterTest(){
        Mockito.reset(
                spyPermissionApiClient,
                spyRoleXPermissionApiClient,
                spyRoleApiClient,
                spyUserApiClient,
                spyOrderApiClient,
                spyOrderElementApiClient,
                spyAddressApiClient,
                spyCityApiClient,
                spyCodeStoreApiClient,
                spyCustomerApiClient,
                spyEmployeeApiClient,
                spyCurrencyApiClient,
                spyProductApiClient,
                spySupplierApiClient
        );

        Mockito.clearInvocations(
                spyPermissionApiClient,
                spyRoleXPermissionApiClient,
                spyRoleApiClient,
                spyUserApiClient,
                spyOrderApiClient,
                spyOrderElementApiClient,
                spyAddressApiClient,
                spyCityApiClient,
                spyCodeStoreApiClient,
                spyCustomerApiClient,
                spyEmployeeApiClient,
                spyCurrencyApiClient,
                spyProductApiClient,
                spySupplierApiClient
        );
        logger.info("Mockito reset and clear happened");
    }

    @AfterSuite
    protected void destroy(){
        if(driver != null){
            driver.quit();
        }
    }
}

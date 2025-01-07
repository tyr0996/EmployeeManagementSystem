package hu.martin.ems;

import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.service.CurrencyService;
import hu.martin.ems.vaadin.api.*;
import hu.martin.ems.vaadin.component.ComponentManager;
import org.mockito.Mockito;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import javax.sql.DataSource;
import java.io.File;
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
    protected static CurrencyService spyCurrencyService;

    @Autowired
    @SpyBean
    protected static RestTemplate spyRestTemplate;

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
    protected static SupplierApiClient spySupplierApiClient;

    @SpyBean
    @Autowired
    protected static UserApiClient spyUserApiClient;

//    @Spy
//    @Autowired
//    protected static EntityManager spyEntityManager;

    @SpyBean
    @Autowired
    protected static DataSource spyDataSource;

//    @SpyBean
//    @Autowired
//    protected static BaseRepositoryImpl<User, Long> spyUserSimpleJpaRepository;


    protected static String fetchingCurrencyApiUrl;

    protected static String baseCurrency;

    protected static DataSource originalDataSource;


    @BeforeSuite(alwaysRun = true)
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();
        originalDataSource = BeanProvider.getBean(DataSource.class);
        downloadPath = env.getProperty("chrome.download.path");
        fetchingCurrencyApiUrl = env.getProperty("api.currency.url");
        baseCurrency = env.getProperty("api.currency.baseCurrency");

        clearDownloadFolder();

        ChromeOptions options = new ChromeOptions();
        HashMap<String, Object> chromePref = new HashMap<>();

        chromePref.put("download.default_directory", downloadPath);
        chromePref.put("download.prompt_for_download", false);
        chromePref.put("directory_upgrade", true);
        options.setExperimentalOption("prefs", chromePref);
        chromePref.put("plugins.always_open_pdf_externally", true);



        //options.addArguments("--headless");

        driver = new ChromeDriver(options);
        port = webServerAppCtxt.getWebServer().getPort();
        driver.manage().window().setPosition(new Point(1280, -760));
        driver.manage().window().maximize();
        dp = dataProvider;
    }

    private void clearDownloadFolder(){
        File dir = new File(downloadPath);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if(!file.delete()){
                    System.err.println("The file deletion failed: " + file.getName());
                }
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    @BeforeMethod(alwaysRun = true)
    public void afterTest(){
        Mockito.reset(
                spyPermissionApiClient,
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
                spySupplierApiClient,
                spyDataSource
        );

        Mockito.clearInvocations(
                spyPermissionApiClient,
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
                spySupplierApiClient,
                spyDataSource
        );

//        spyDataSource = originalDataSource;
        logger.info("Mockito reset and clear happened");
    }

    @AfterSuite
    protected void destroy(){
        if(driver != null){
            driver.quit();
        }
    }
}

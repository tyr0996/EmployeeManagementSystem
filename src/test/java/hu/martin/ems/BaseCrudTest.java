package hu.martin.ems;

import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.service.UserService;
import hu.martin.ems.service.*;
import hu.martin.ems.vaadin.api.EmailSendingApi;
import hu.martin.ems.vaadin.component.ComponentManager;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
import java.io.IOException;
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

    @SpyBean
    protected static CurrencyService spyCurrencyService;

    @SpyBean
    protected static RestTemplate spyRestTemplate;

    @SpyBean
    protected static ComponentManager spyComponentManager;

    @SpyBean
    protected static EmailSendingApi spyEmailSendingApi;

    @SpyBean
    protected static CityService spyCityService;
    
    @SpyBean
    protected static DataSource spyDataSource;

    @SpyBean
    public static OrderService spyOrderService;

    @SpyBean
    protected static CodeStoreService spyCodeStoreService;

    @SpyBean
    protected static PermissionService spyPermissionService;

    @SpyBean
    protected static CustomerService spyCustomerService;

    @SpyBean
    protected static EmployeeService spyEmployeeService;

    @SpyBean
    protected static RoleService spyRoleService;

    @SpyBean
    protected static SupplierService spySupplierService;

    @SpyBean
    protected static ProductService spyProductService;

    @SpyBean
    protected static AddressService spyAddressService;

    @SpyBean
    protected static OrderElementService spyOrderElementService;

    @SpyBean
    protected static UserService spyUserService;

    protected static String fetchingCurrencyApiUrl;

    protected static String baseCurrency;

    protected static DataSource originalDataSource;


    @BeforeSuite(alwaysRun = true)
//    @BeforeSuite
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();
        MockitoAnnotations.openMocks(this);
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
        resetServicesMock();
        clearInvocationsInServices();
        Mockito.reset(spyComponentManager);
        Mockito.reset(spyRestTemplate);
        Mockito.clearInvocations(spyComponentManager);
        Mockito.clearInvocations(spyRestTemplate);



//        spyDataSource = originalDataSource;
        logger.info("Mockito reset and clear happened");
    }

    private void resetServicesMock(){
        Mockito.reset(
                spyPermissionService,
                spyRoleService,
                spyUserService,
                spyOrderService,
                spyOrderElementService,
                spyAddressService,
                spyCityService,
                spyCustomerService,
                spyEmployeeService,
                spyCurrencyService,
                spyProductService,
                spySupplierService,
                spyDataSource,
                spyOrderService,
                spyCodeStoreService
        );
    }

    private void clearInvocationsInServices(){
        Mockito.clearInvocations(
                spyPermissionService,
                spyRoleService,
                spyUserService,
                spyOrderService,
                spyOrderElementService,
                spyAddressService,
                spyCityService,
                spyCustomerService,
                spyEmployeeService,
                spyCurrencyService,
                spyProductService,
                spySupplierService,
                spyDataSource,
                spyOrderService,
                spyCodeStoreService
        );
    }

    public void resetDatabase() throws IOException {
        dp.resetDatabase();
        System.out.println("database reseted");
    }

    @AfterSuite
    protected void destroy(){
        if(driver != null){
            driver.quit();
        }
    }
}

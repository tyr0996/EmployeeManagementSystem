package hu.martin.ems;

import hu.martin.ems.base.selenium.WebDriverProvider;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.config.JPAConfig;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.service.EmailSendingService;
import hu.martin.ems.service.AdminToolsService;
import hu.martin.ems.service.CurrencyService;
import hu.martin.ems.service.OrderService;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestTemplate;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.*;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseCrudTest extends AbstractTestNGSpringContextTests implements ITestListener {

    //TODO megcsinálni, hogy ha módosítottunk egy elemet vagy valamit, akkor ellenőrizzük a létrehozás gombot! fontos, hogy üres legyen a form és létrehozás legyen a címben!

    public static final String contentXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]";
//    protected static WebDriver driver;

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
    protected DataProvider dataProvider;

    private Logger logger = LoggerFactory.getLogger(BaseCrudTest.class);

    @Autowired
    private Environment env;

    protected static Integer port;
    protected static DataProvider dp;
    public static String screenshotPath;

//    protected static WebDriver driver;
    
    private ThreadLocal<WebDriver> threadLocalWebDriver = new ThreadLocal<>();
    
    public void setDriver(WebDriver driver){
        threadLocalWebDriver.set(driver);
    }
    
    public WebDriver getDriver(){
        return threadLocalWebDriver.get();
    }



    @SpyBean
    protected static CurrencyService spyCurrencyService;

    @SpyBean
    protected static RestTemplate spyRestTemplate;

    @SpyBean
    protected static DataSource spyDataSource;

    @SpyBean //Ide mindenképp kell a SpyBean, mert különben nem működik a dokumentum-generálól mock!
    public static OrderService spyOrderService;

    @SpyBean
    public static AdminToolsService spyAdminToolsService;

    ThreadLocal<AdminToolsService> adminToolsServiceThreadLocal = new ThreadLocal<>();

    @SpyBean
    protected static EmailSendingService spyEmailSendingService;

    protected static String fetchingCurrencyApiUrl;

    protected static String baseCurrency;
    protected static String databaseLogPath;

    protected static DataSource originalDataSource;


    @BeforeSuite(alwaysRun = true)
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();

        DataProvider.saveAllSqlsFromJsons();

        MockitoAnnotations.openMocks(this);
        originalDataSource = BeanProvider.getBean(DataSource.class);
        screenshotPath = env.getProperty("selenium.screenshot.folder");
        fetchingCurrencyApiUrl = env.getProperty("api.currency.url");
        baseCurrency = env.getProperty("api.currency.baseCurrency");
        databaseLogPath = env.getProperty("database.logpath");

        clearDownloadFolder();
        clearScreenshotFolder();
        
        port = webServerAppCtxt.getWebServer().getPort();
        dp = dataProvider;
    }

    private void clearFolder(String folderPath){
        File dir = new File(folderPath);
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if(!file.delete()){
                    System.err.println("The file deletion failed: " + file.getName());
                }
            }
        }
    }

    private void clearDownloadFolder(){
        clearFolder(StaticDatas.Selenium.downloadPath);
    }

    private void clearScreenshotFolder(){
        clearFolder(screenshotPath);
    }

    private void clearEnvironment(){
        JPAConfig.resetCallIndex();
//        resetServicesMock();
//        clearInvocationsInServices();
//        logger.debug("Mockito reset and clear happened");
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestResult result){
        clearEnvironment();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(){
        clearEnvironment();
    }

//    private void resetServicesMock(){
//        Mockito.reset(spyCurrencyService);
//        Mockito.reset(spyDataSource);
//        Mockito.reset(spyRestTemplate);
//    }
//
//    private void clearInvocationsInServices(){
//        Mockito.clearInvocations(spyCurrencyService);
//        Mockito.clearInvocations(spyDataSource);
//        Mockito.clearInvocations(spyRestTemplate);
//    }

    public void resetDatabase() throws IOException {
        dp.resetDatabase();
        logger.info("Database reseted");
    }

    @AfterSuite
    protected void destroy() throws InterruptedException {
        //TODO megcsinálni, hogy zárja be.
//        if(driver != null){
//            getDriver().quit();
//        }
    }

    protected void resetRolesAndPermissions(){
        dp.executeSQL("DELETE FROM roles_permissions CASCADE");
        dp.executeSQL("ALTER SEQUENCE roles_permissions_permission_id_seq RESTART WITH 1");
        dp.executeSQL("ALTER SEQUENCE roles_permissions_role_id_seq RESTART WITH 1");

        dp.executeSQL("BEGIN; SET session_replication_role = 'replica'; DELETE FROM role; SET session_replication_role = 'origin'; COMMIT;");
        dp.executeSQL("ALTER SEQUENCE role_id_seq RESTART WITH 3"); //TODO Erre azért van szükség, mert van a json-ben id, így nem lépteti automatikusan a számlálót, és hibát fog írni mentésnél

        dp.executeSQL("DELETE FROM permission CASCADE");
        dp.executeSQL("ALTER SEQUENCE permission_id_seq RESTART WITH 1");

        dp.executeSQLFile(new File(StaticDatas.FolderPaths.GENERATED_SQL_FILES_PATH + "\\roles.sql"));
        dp.executeSQLFile(new File(StaticDatas.FolderPaths.GENERATED_SQL_FILES_PATH + "\\permissions.sql"));
        dp.executeSQLFile(new File(StaticDatas.FolderPaths.GENERATED_SQL_FILES_PATH + "\\rolexpermissions.sql"));

        JPAConfig.resetCallIndex();
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass(){
        setupBrowser();
    }

    private void setupBrowser() {
        setDriver(new WebDriverProvider().get());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        //TODO megcsinálni a fényképezést
//        Object instance = result.getInstance();
//        WebDriver instanceDriver = ((BaseCrudTest) instance).getDriver();
//        GridTestingUtil instanceGridTestingUtil = ((BaseCrudTest) instance).getGridTestingUtil();
//        if (instanceDriver != null) {
//            instanceGridTestingUtil.takeScreenshot(instanceDriver);
//        }
    }

    protected void resetUsers() {
        dp.executeSQL("BEGIN; SET session_replication_role = 'replica'; DELETE FROM loginuser; SET session_replication_role = 'origin'; COMMIT;");
        dp.executeSQL("ALTER SEQUENCE loginuser_id_seq RESTART WITH 1");
        dp.executeSQL("INSERT INTO loginuser (deleted, username, passwordHash, role_role_id, enabled) VALUES ('0', 'admin', '$2a$12$Ei2ntwIK/6lePBO2UecedetPpxxDee3kmxnkWTXZI.CiPb86vejHe', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1), true)");
        dp.executeSQL("INSERT INTO loginuser (deleted, username, passwordHash, role_role_id, enabled) VALUES ('0', 'robi', '$2a$12$/LIbE6V8xP/2frZmSbe5.OSMyqiIbwQEau0nNsGk./P2PXP1M8BFi', (SELECT id as role_role_id FROM Role WHERE id = 2 LIMIT 1), true)");
        dp.executeSQL("INSERT INTO loginuser (deleted, username, passwordHash, role_role_id, enabled) VALUES ('0', 'Erzsi', '$2a$12$4Eb.fZ748irmUDwJl1NueO6CjrVLFiP0E41qx3xsE6KAYxx00IfrG', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1), false)");
        logger.info("All user recovered");
        JPAConfig.resetCallIndex();
    }

    @AfterClass(alwaysRun = true)
    public void afterClass() {
        WebDriver driver = getDriver();
        if (driver != null) {
            driver.quit();
            threadLocalWebDriver.remove();
        }
    }
}

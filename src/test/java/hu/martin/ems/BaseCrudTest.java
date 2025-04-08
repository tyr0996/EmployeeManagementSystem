package hu.martin.ems;

import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import hu.martin.ems.base.selenium.ScreenshotMaker;
import hu.martin.ems.base.selenium.WebDriverProvider;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.config.JPAConfig;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.controller.EndpointController;
import hu.martin.ems.core.service.EmailSendingService;
import hu.martin.ems.core.sftp.SftpSender;
import hu.martin.ems.service.AdminToolsService;
import hu.martin.ems.service.CurrencyService;
import hu.martin.ems.service.OrderService;
import org.mockito.Mockito;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 <table border="1">
     <thead>
         <tr>
             <th>Felhasználónév</th>
             <th>Jelszó</th>
         </tr>
     </thead>
     <tbody>
         <tr>
             <td>admin</td>
             <td>29b{}'f<0V>Z</td>
         </tr>
         <tr>
             <td>robi</td>
             <td>3W-@s2|0^x&Y</td>
         </tr>
         <tr>
             <td>Erzsi</td>
             <td>&B0sEh3U5m;L</td>
         </tr>
     </tbody>
 </table>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseCrudTest extends AbstractTestNGSpringContextTests implements ITestListener {

    //TODO megcsinálni, hogy ha módosítottunk egy elemet vagy valamit, akkor ellenőrizzük a létrehozás gombot! fontos, hogy üres legyen a form és létrehozás legyen a címben!

    public static final String contentXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]";
//    protected static WebDriver driver;

    @Autowired
    protected ServletWebServerApplicationContext webServerAppCtxt;

    @Autowired
    protected DataProvider dataProvider;

    @SpyBean
    protected static BeanProvider spyBeanProvider;

    private Logger logger = LoggerFactory.getLogger(BaseCrudTest.class);

    @Autowired
    private Environment env;

    protected static Integer port;
    protected static DataProvider dp;
    public static String screenshotPath;

    protected static WebDriver driver;


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

    @SpyBean
    protected static EmailSendingService spyEmailSendingService;

    @SpyBean
    public static XDocReportRegistry spyRegistry;

    @SpyBean
    public static EndpointController spyEndpointController;

//    @InjectMocks
    @SpyBean
    public static SftpSender sftpSender;

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
        
        driver = WebDriverProvider.get();
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
        resetMock();
    }

    protected void resetMock(){
        clearInvocationsInServices();
        resetServicesMock();
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(){
        clearEnvironment();
    }

    private void resetServicesMock(){
        Mockito.reset(spyCurrencyService);
        Mockito.reset(spyDataSource);
        Mockito.reset(spyRestTemplate);
        Mockito.reset(spyOrderService);
        Mockito.reset(spyAdminToolsService);
        Mockito.reset(spyEmailSendingService);
        Mockito.reset(spyEndpointController);
        System.out.println("Mockito reseting done!");
    }

    private void clearInvocationsInServices(){
        Mockito.clearInvocations(spyCurrencyService);
        Mockito.clearInvocations(spyDataSource);
        Mockito.clearInvocations(spyRestTemplate);
        Mockito.clearInvocations(spyOrderService);
        Mockito.clearInvocations(spyAdminToolsService);
        Mockito.clearInvocations(spyEmailSendingService);
        Mockito.clearInvocations(spyEndpointController);
        System.out.println("Mockito invocation clearing done!");

    }

    public void resetDatabase() throws IOException {
        dp.resetDatabase();
        logger.info("Database reseted");
    }

    @AfterSuite
    protected void destroy() {
        if(driver != null){
            driver.quit();
        }
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

    @Override
    public void onTestFailure(ITestResult result) {
        //TODO megcsinálni a fényképezést
        Object instance = result.getInstance();
        WebDriver instanceDriver = ((BaseCrudTest) instance).driver;

        if (instanceDriver != null) {
            ScreenshotMaker.takeScreenshot(instanceDriver);
        }
    }

    protected void resetUsers() {
        dp.executeSQL("BEGIN; SET session_replication_role = 'replica'; DELETE FROM loginuser; SET session_replication_role = 'origin'; COMMIT;");
        dp.executeSQL("ALTER SEQUENCE loginuser_id_seq RESTART WITH 1");
        dp.executeSQL("INSERT INTO loginuser (deleted, username, passwordHash, role_role_id, enabled) VALUES ('0', 'admin', '$2a$12$21wsdBKKqiHILOElhmEhGe3R11QIlrXmA6xlY.CowoExz8rlxB9Bu', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1), true)");
        dp.executeSQL("INSERT INTO loginuser (deleted, username, passwordHash, role_role_id, enabled) VALUES ('0', 'robi', '$2a$12$ENKhjamGSnSXx81f0IRPQObhyEOccAbutpkjJRai0.dshqFRyFETy', (SELECT id as role_role_id FROM Role WHERE id = 2 LIMIT 1), true)");
        dp.executeSQL("INSERT INTO loginuser (deleted, username, passwordHash, role_role_id, enabled) VALUES ('0', 'Erzsi', '$2a$12$XGHOnxr5AyfmOoIjKEEP7.JXIXZgNiB53uf2AhbpwdAFztqi8FqCy', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1), false)");
        logger.info("All user recovered");
        JPAConfig.resetCallIndex();
    }

    protected void resetCustomers(){
        //TODO: meg kellene csinálni, hogy a JSON-ből vegye ki az SQL-t, és ne így egyesével kelljen megírni

        dp.executeSQL("BEGIN; SET session_replication_role = 'replica'; DELETE FROM customer; SET session_replication_role = 'origin'; COMMIT;");
        dp.executeSQL("ALTER SEQUENCE customer_id_seq RESTART WITH 1");
        dp.executeSQL("INSERT INTO customer (deleted, firstName, lastName, emailAddress, address_address_id) VALUES ('0', 'Erdei', 'Róbert', 'robert.emailcime@gmail.com', '1')");
        dp.executeSQL("INSERT INTO customer (deleted, firstName, lastName, emailAddress, address_address_id) VALUES ('0', 'Gálber', 'Martin', 'martin.emailcime@gmail.com', '2')");
//        dp.executeSQL("INSERT INTO loginuser (deleted, username, passwordHash, role_role_id, enabled) VALUES ('0', 'Erzsi', '$2a$12$4Eb.fZ748irmUDwJl1NueO6CjrVLFiP0E41qx3xsE6KAYxx00IfrG', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1), false)");
        logger.info("All customer recovered");
        JPAConfig.resetCallIndex();
    }

    @AfterSuite(alwaysRun = true)
    public void afterClass() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected boolean waitForDownload(String fileNameRegex, int times, int padding) throws Exception {
        Pattern pattern = Pattern.compile(fileNameRegex);

        for (int i = 0; i < times; i++) {
            File dir = new File(StaticDatas.Selenium.downloadPath);
            File[] files = dir.listFiles();

            if (files != null) {
                for (File file : files) {
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.matches()) {
                        if (file.delete()) {
                            return true;
                        }
                    }
                }
            }
            Thread.sleep(padding); // Várakozás 1 másodpercig
        }
        return false;
    }
}

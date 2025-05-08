package hu.martin.ems;

import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import hu.martin.ems.base.selenium.ScreenshotMaker;
import hu.martin.ems.base.selenium.WebDriverProvider;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.config.*;
import hu.martin.ems.core.controller.EndpointController;
import hu.martin.ems.core.schedule.CurrencyScheduler;
import hu.martin.ems.core.service.EmailSendingService;
import hu.martin.ems.core.sftp.SftpSender;
import hu.martin.ems.model.Currency;
import hu.martin.ems.service.AdminToolsService;
import hu.martin.ems.service.CurrencyService;
import hu.martin.ems.service.OrderService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
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
import org.testng.annotations.*;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
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
@Listeners(BaseCrudTest.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BaseCrudTest extends AbstractTestNGSpringContextTests implements ITestListener {
    private static AtomicInteger passedCount = new AtomicInteger(0);
    private static AtomicInteger failedCount = new AtomicInteger(0);
    private static AtomicInteger skippedCount = new AtomicInteger(0);

    @Override
    public void onTestSuccess(ITestResult result) {
        passedCount.set(passedCount.incrementAndGet());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        skippedCount.incrementAndGet();
    }


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
    public static IconProvider spyIconProvider;

    @SpyBean
    protected static EmailSendingService spyEmailSendingService;

    @SpyBean
    protected static  CurrencyScheduler spyCurrencyScheduler;


    @SpyBean
    public static XDocReportRegistry spyRegistry;

    @SpyBean
    public static EndpointController spyEndpointController;

    @SpyBean
    public static JschConfig spyJschConfig;

//    @InjectMocks
    @SpyBean
    public static SftpSender sftpSender;

    @Autowired
    public WebDriverProvider driverProvider;

    protected static String fetchingCurrencyApiUrl;

    protected static String baseCurrency;
    protected static String databaseLogPath;

    protected static DataSource originalDataSource;

    @BeforeSuite(alwaysRun = true)
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();

        dataProvider.saveAllSqlsFromJsons();

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
        
        driver = BeanProvider.getBean(WebDriverProvider.class).get();
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
        clearFolder(BeanProvider.getBean(WebDriverProvider.class).getDownloadFolder());
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
    public void beforeMethod() throws IOException {
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
        Mockito.reset(spyJschConfig);
        Mockito.reset(spyIconProvider);
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
        Mockito.clearInvocations(spyJschConfig);
        Mockito.clearInvocations(spyIconProvider);
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

    protected void resetRolesAndPermissions() throws IOException {
        dp.executeSQL("DELETE FROM roles_permissions CASCADE");
        dp.executeSQL("ALTER SEQUENCE roles_permissions_permission_id_seq RESTART WITH 1");
        dp.executeSQL("ALTER SEQUENCE roles_permissions_role_id_seq RESTART WITH 1");

        dp.executeSQL("BEGIN; SET session_replication_role = 'replica'; DELETE FROM role; SET session_replication_role = 'origin'; COMMIT;");
        dp.executeSQL("ALTER SEQUENCE role_id_seq RESTART WITH 3"); //TODO Erre azért van szükség, mert van a json-ben id, így nem lépteti automatikusan a számlálót, és hibát fog írni mentésnél

        dp.executeSQL("DELETE FROM permission CASCADE");
        dp.executeSQL("ALTER SEQUENCE permission_id_seq RESTART WITH 1");

        dp.executeSQLFile(new File(dp.getGENERATED_SQL_FILES_PATH() + "\\roles.sql"));
        dp.executeSQLFile(new File(dp.getGENERATED_SQL_FILES_PATH() + "\\permissions.sql"));
        dp.executeSQLFile(new File(dp.getGENERATED_SQL_FILES_PATH() + "\\rolexpermissions.sql"));

        JPAConfig.resetCallIndex();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        failedCount.incrementAndGet();
        if (driver != null) {
            new ScreenshotMaker().takeScreenshot(driver);
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
    public void afterClass() throws IOException {
        if (driver != null) {
            driver.quit();
        }
        sendEmail();
    }

    protected void clearCurrencyDatabaseTable(){
        List<Currency> currentCurrencies = spyCurrencyService.findAll(false);
        currentCurrencies.forEach(v -> {
            spyCurrencyService.forcePermanentlyDelete(v.getId());
        });
    }

    protected boolean waitForDownload(String fileNameRegex, int times, int padding) throws Exception {
        Pattern pattern = Pattern.compile(fileNameRegex);

        for (int i = 0; i < times; i++) {
            File dir = new File(BeanProvider.getBean(WebDriverProvider.class).getDownloadFolder());
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

    private void sendEmail() {
        final String fromEmail = "emstestreporter@gmail.com";
        final String password = "hdmt jmer cpce wutv";

        String toEmail = "galmar05aszi@gmail.com";

        String subject = "Tesztek futása";
        int success = passedCount.get();
        int failed = failedCount.get();
        int skipped = skippedCount.get();
        int all = success + failed + skipped;
        String body = "A tesztek futása befejeződött. Ez egy automatikus üzenet, amit a BaseCrudTest generált.\n\nÖsszes: " + all + "\nSikeres: " + success + "\nSikertelen: " + failed + "\nKihagyott: " + skipped;

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            System.out.println("E-mail sikeresen elküldve.");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}

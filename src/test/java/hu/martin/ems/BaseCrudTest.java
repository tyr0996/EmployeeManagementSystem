package hu.martin.ems;

import hu.martin.ems.core.config.DataProvider;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.*;

import java.util.HashMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BaseCrudTest extends AbstractTestNGSpringContextTests {

    protected static WebDriver driver;

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;
    @Autowired
    protected DataProvider dataProvider;
    protected static Integer port;
    protected static DataProvider dp;

    @Value("${chrome.download.path}")
    private String downloadPath;

    @BeforeSuite(alwaysRun = true)
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();

        ChromeOptions options = new ChromeOptions();
        HashMap<String, Object> chromePref = new HashMap<>();
        chromePref.put("download.default_directory", downloadPath);
        chromePref.put("download.prompt_for_download", false);
        chromePref.put("directory_upgrade", true);
        options.setExperimentalOption("prefs", chromePref);

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        port = webServerAppCtxt.getWebServer().getPort();
        dp = dataProvider;
    }

    @AfterSuite
    protected void destroy(){
        if(driver != null){
            driver.quit();
        }
    }
}

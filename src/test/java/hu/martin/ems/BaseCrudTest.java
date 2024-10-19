package hu.martin.ems;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.crudFE.CurrencyCrudTest;
import hu.martin.ems.repository.CurrencyRepository;
import hu.martin.ems.service.CurrencyService;
import lombok.Getter;
import org.atmosphere.config.service.Get;
import org.mockito.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.mock.mockito.SpyBeans;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.*;

import java.util.HashMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class BaseCrudTest extends AbstractTestNGSpringContextTests {

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

    protected static String fetchingCurrencyApiUrl;

    protected static String baseCurrency;

//    @Autowired
//    @SpyBean
//    protected static ObjectMapper om;




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

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        port = webServerAppCtxt.getWebServer().getPort();
        dp = dataProvider;
//
//        MockitoAnnotations.openMocks(CurrencyCrudTest.class);
    }

    @AfterSuite
    protected void destroy(){
        if(driver != null){
            driver.quit();
        }
    }
}

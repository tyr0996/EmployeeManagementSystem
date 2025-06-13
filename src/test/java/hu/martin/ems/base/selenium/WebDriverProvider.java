package hu.martin.ems.base.selenium;

import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;

@Component
public class WebDriverProvider {
    @Getter
    @Value("${selenium.download.folder}")
    private String downloadFolder;
    private WebDriver driver;

    @Value("${selenium.webdriver.headless}")
    private boolean headless;

    @PostConstruct
    private void init() {
        String temp = downloadFolder.replaceAll("/", "\\\\");
        downloadFolder = temp;
    }

    public WebDriver get() {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            HashMap<String, Object> chromePref = new HashMap<>();

            chromePref.put("download.default_directory", downloadFolder);
            chromePref.put("download.prompt_for_download", false);
            chromePref.put("directory_upgrade", true);
            chromePref.put("plugins.always_open_pdf_externally", true);

            options.setExperimentalOption("prefs", chromePref);
            if (headless) {
                options.addArguments("--headless");
            }
            options.addArguments(
                    "--disable-gpu",
                    "--ignore-certificate-errors",
                    "--no-sandbox",
                    "--disable-dev-shm-usage"
//                    "--headless"
            );

            WebDriver d = new ChromeDriver(options);
            d.manage().window().maximize();
            driver = d;
        }
        return driver;
    }
}

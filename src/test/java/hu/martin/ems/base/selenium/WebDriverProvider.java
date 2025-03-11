package hu.martin.ems.base.selenium;

import hu.martin.ems.core.config.StaticDatas;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;

public class WebDriverProvider {
    public WebDriverProvider(){}

    public WebDriver get(){
        ChromeOptions options = new ChromeOptions();
        HashMap<String, Object> chromePref = new HashMap<>();

        chromePref.put("download.default_directory", StaticDatas.Selenium.downloadPath);
        chromePref.put("download.prompt_for_download", false);
        chromePref.put("directory_upgrade", true);
        options.setExperimentalOption("prefs", chromePref);
        chromePref.put("plugins.always_open_pdf_externally", true);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().setPosition(new Point(1280, -760));
        driver.manage().window().maximize();

        return driver;
    }
}

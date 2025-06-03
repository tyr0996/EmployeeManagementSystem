package hu.martin.ems.pages.core;

import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class VaadinPage {
    @Getter
    private WebDriver driver;
    @Getter
    private int port;
    @Getter
    private WebDriverWait wait;

    public VaadinPage(WebDriver driver, int port) {
        this.driver = driver;
        this.port = port;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(2), Duration.ofMillis(10));
    }
}

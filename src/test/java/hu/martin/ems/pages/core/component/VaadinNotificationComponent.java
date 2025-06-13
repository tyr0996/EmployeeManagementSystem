package hu.martin.ems.pages.core.component;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@Slf4j
public class VaadinNotificationComponent extends VaadinBaseComponent {
    private static final String notificationXpath = "/html/body/vaadin-notification-container/vaadin-notification-card";

    public VaadinNotificationComponent(WebDriver driver) {
        super(driver, By.xpath(notificationXpath));
    }

    public VaadinNotificationComponent(WebDriver driver, Duration duration) {
        super(driver);
        this.provider = By.xpath(notificationXpath);
        this.scope = driver.findElement(By.xpath("/html")); //TODO: lehet, hogy ide kell a /body a html után.
        wait = new WebDriverWait(driver, duration, Duration.ofMillis(10));
        this.element = wait.until(ExpectedConditions.visibilityOfElementLocated(provider));
    }

    public String getText() {
        return element.getText();
    }

    public void close() {
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        js.executeScript("arguments[0].remove();", this.element);
    }

    public static String hasNotification(WebDriver driver) {
        try {
            VaadinNotificationComponent notification = new VaadinNotificationComponent(driver, Duration.ofMillis(1000));
            return notification.getText();
        } catch (TimeoutException ex) {
            return null;
        }
    }

    public static void closeAll(WebDriver driver) {
        closeAll(driver, new WebDriverWait(driver, Duration.ofMillis(100), Duration.ofMillis(10)));
    }

    public static void closeAll(WebDriver driver, WebDriverWait waitForClose) {
        try {
            VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
            notification.close();
            waitForClose.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(notificationXpath)));
            closeAll(driver);
        } catch (TimeoutException ex) {
            log.debug("No more notification found");
        }
    }
}

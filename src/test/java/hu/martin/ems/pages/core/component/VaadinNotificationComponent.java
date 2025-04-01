package hu.martin.ems.pages.core.component;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class VaadinNotificationComponent extends VaadinBaseComponent {
    private static final String notificationXpath = "/html/body/vaadin-notification-container/vaadin-notification-card";

    public VaadinNotificationComponent(WebDriver driver) {
        super(driver, notificationXpath);
    }

    public String getText(){
        return element.getText();
    }

    public void close(){
        JavascriptExecutor js = (JavascriptExecutor) getDriver();
        js.executeScript("arguments[0].remove();", this.element);
    }

    public static boolean hasNotification(WebDriver driver){
        try{
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(1000), Duration.ofMillis(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(notificationXpath)));
            return true;
        }
        catch(TimeoutException ex){
            return false;
        }
    }

    public static void closeAll(WebDriver driver){
        try{
            WebDriverWait waitForClose = new WebDriverWait(driver, Duration.ofMillis(100), Duration.ofMillis(10));
            VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
            notification.close();
            waitForClose.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(notificationXpath)));
            closeAll(driver);
        }
        catch (TimeoutException ex){
        }
    }
}

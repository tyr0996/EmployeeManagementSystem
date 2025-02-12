package hu.martin.ems;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TestingUtils {
    public static WebElement getParent(WebElement element){
        return element.findElement(By.xpath("./.."));
    }

    public static void loginWith(WebDriver driver, Integer port, String username, String password, boolean requiredSuccess){
        driver.get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"input-vaadin-text-field-6\"]")));
        WebElement passwordField = driver.findElement(By.xpath("//*[@id=\"input-vaadin-password-field-7\"]"));
        WebElement loginButton = driver.findElement(By.xpath("//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[1]"));


        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();

        if(requiredSuccess){
            WebDriverWait loginWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            loginWait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout")));
        }
    }

    public static void loginWith(WebDriver driver, Integer port, String username, String password) throws InterruptedException {
        loginWith(driver, port, username, password, true);

        //*[@id="ROOT-2521314"]/vaadin-horizontal-layout/vaadin-vertical-layout/vaadin-horizontal-layout[1]/span

    }
}

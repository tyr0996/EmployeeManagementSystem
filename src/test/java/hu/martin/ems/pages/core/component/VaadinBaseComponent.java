package hu.martin.ems.pages.core.component;

import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class VaadinBaseComponent implements IVaadinBaseComponent {
    @Getter
    protected WebElement element;

    @Getter
    private final WebDriverWait wait;

    @Getter
    private final WebDriverWait refreshWait;
    public VaadinBaseComponent(WebDriver driver, WebElement element) {
        this.wait = new WebDriverWait(driver, Duration.ofMillis(2000), Duration.ofMillis(10));
        this.refreshWait = new WebDriverWait(driver, Duration.ofMillis(200), Duration.ofMillis(10));
        this.element = element;
    }
    public VaadinBaseComponent(WebDriver driver, String elementXpath){
        this.wait = new WebDriverWait(driver, Duration.ofMillis(2000), Duration.ofMillis(10));
        this.refreshWait = new WebDriverWait(driver, Duration.ofMillis(200), Duration.ofMillis(10));
        this.element = this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(elementXpath)));
    }
    public VaadinBaseComponent(WebDriver driver){
        this.refreshWait = new WebDriverWait(driver, Duration.ofMillis(200), Duration.ofMillis(10));
        this.wait = new WebDriverWait(driver, Duration.ofMillis(2000), Duration.ofMillis(10));
    }

    public void waitForRefresh(){
        try{
            refreshWait.until(ExpectedConditions.stalenessOf(element));
        }
        catch (TimeoutException e){

        }
    }



    public boolean isNull(){
        return this.element == null;
    }

    public Boolean isEnabled(){
        return element.getDomAttribute("disabled") == null;
    }

    public WebElement getParentWebElement(WebElement element){
        return element.findElement(By.xpath("./.."));
    }

    public void printToConsole(){
        System.out.println(element.getAttribute("outerHTML"));
    }

    public static void printToConsole(WebElement element){
        System.out.println(element.getAttribute("outerHTML"));
    }
}

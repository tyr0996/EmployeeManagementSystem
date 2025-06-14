package hu.martin.ems.pages.core.component;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public abstract class VaadinBaseComponent implements IVaadinBaseComponent {
    @Getter
    protected WebElement element;

    @Getter
    protected WebDriverWait wait;

    @Getter
    private final WebDriverWait refreshWait;

    @Getter
    protected WebElement scope;

    protected By provider;

    public VaadinBaseComponent(WebDriver driver, By provider) {
        this(driver);
        this.provider = provider;
        this.scope = driver.findElement(By.xpath("/html")); //TODO: lehet, hogy ide kell a /body a html után.
        init();
    }

    public VaadinBaseComponent(WebDriver driver, WebElement scope, By provider, int index) {
        this(driver);
        this.provider = provider;
        this.scope = scope;
        this.element = this.wait.until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(scope, provider)).get(index);
    }

    public VaadinBaseComponent(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10), Duration.ofMillis(10)); // TODO ezt visszavenni 2-re
        this.refreshWait = new WebDriverWait(driver, Duration.ofMillis(200), Duration.ofMillis(10));
    }

    public void init() {
        this.element = this.wait.until(ExpectedConditions.visibilityOfElementLocated(provider));
    }

    public void waitForRefresh() {
        try {
            refreshWait.until(ExpectedConditions.stalenessOf(element));
        } catch (TimeoutException e) {

        }
    }

    protected List<WebElement> getAllChildren() {
        return element.findElements(By.xpath("./*"));
    }


    public boolean isNull() {
        return this.element == null;
    }

    public Boolean isEnabled() {
        return element.getDomAttribute("disabled") == null;
    }

    public WebElement getParentWebElement(WebElement element) {
        return element.findElement(By.xpath("./.."));
    }

    public void printToConsole() {
        System.out.println(element.getAttribute("outerHTML"));
    }

    public static void printToConsole(WebElement element) {
        System.out.println(element.getAttribute("outerHTML"));
    }
}

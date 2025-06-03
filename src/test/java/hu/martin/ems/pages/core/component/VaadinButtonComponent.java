package hu.martin.ems.pages.core.component;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VaadinButtonComponent extends VaadinBaseComponent {
    public VaadinButtonComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    public VaadinButtonComponent(WebDriver driver, WebElement parent, By provider, int index) {
        super(driver, parent, provider, index);
    }

    public VaadinButtonComponent(WebDriver driver, String elementXpath) {
        super(driver, By.xpath(elementXpath));
    }

    public void click() {
        element.click();
    }

    public void forceClick() {
        ((JavascriptExecutor) getDriver()).executeScript("arguments[0].click()", element);
    }
}

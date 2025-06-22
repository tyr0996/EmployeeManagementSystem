package hu.martin.ems.pages.core.component;

import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.SingleFillable;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Random;

public class VaadinSwitchComponent extends VaadinFillableComponent implements SingleFillable<VaadinSwitchComponent, Boolean> {

    public VaadinSwitchComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    public VaadinSwitchComponent(WebDriver driver, WebElement scope, By provider, int index) {
        super(driver, scope, provider, index);
    }

    @Override
    public String getTitle(){
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return (String) js.executeScript("return arguments[0].querySelector('vaadin-horizontal-layout').querySelectorAll('span')[0].textContent", element);
    }

    public boolean getStatus() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        Boolean o = (Boolean) js.executeScript("return arguments[0].querySelector('vaadin-horizontal-layout').querySelectorAll('span')[1].classList.contains('on')", element);
        return o;
    }

    public void setStatus(boolean newStatus) {
        Boolean status = getStatus();
        if (status != newStatus) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].querySelector('vaadin-horizontal-layout').querySelectorAll('span')[1].click()", element);
            waitForRefresh();
        }
    }

    @Override
    public VaadinSwitchComponent fillWith(Boolean value) {
        setStatus(value);
        return this;
    }

    @Override
    public VaadinSwitchComponent fillWithRandom() {
        setStatus(new Random().nextBoolean());
        return this;
    }
}

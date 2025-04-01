package hu.martin.ems.pages.core.component;

import hu.martin.ems.pages.core.component.saveOrUpdateDialog.SingleFillable;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class VaadinSimpleInputComponent<T, S> extends VaadinFillableComponent implements SingleFillable<T, S> {
    public VaadinSimpleInputComponent(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void clear(){
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
    }
}

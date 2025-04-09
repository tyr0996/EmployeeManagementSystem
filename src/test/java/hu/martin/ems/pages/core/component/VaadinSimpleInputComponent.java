package hu.martin.ems.pages.core.component;

import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.SingleFillable;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public abstract class VaadinSimpleInputComponent<T, S> extends VaadinFillableComponent implements SingleFillable<T, S> {
    public VaadinSimpleInputComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    public void clear(){
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
    }
}

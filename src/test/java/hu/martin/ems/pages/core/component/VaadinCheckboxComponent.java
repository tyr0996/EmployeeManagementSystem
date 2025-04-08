package hu.martin.ems.pages.core.component;

import hu.martin.ems.pages.core.component.saveOrUpdateDialog.SingleFillable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Random;

public class VaadinCheckboxComponent extends VaadinFillableComponent implements SingleFillable<VaadinCheckboxComponent, Boolean> {

    public VaadinCheckboxComponent(WebDriver driver, By provider){
        super(driver, provider);
    }

    public VaadinCheckboxComponent(WebDriver driver, WebElement scope, By provider, int index){
        super(driver, scope, provider, index);
    }

    public boolean getStatus(){
        return element.getDomAttribute("checked") != null;
    }

    public void setStatus(boolean newStatus){
        Boolean status = getStatus();
        if(status != newStatus){
            element.click();
        }
    }

    @Override
    public VaadinCheckboxComponent fillWith(Boolean value) {
        setStatus(value);
        return this;
    }

    @Override
    public VaadinCheckboxComponent fillWithRandom() {
        setStatus(new Random().nextBoolean());
        return this;
    }
}

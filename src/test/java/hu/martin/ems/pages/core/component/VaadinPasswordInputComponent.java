package hu.martin.ems.pages.core.component;

import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.SingleFillable;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class VaadinPasswordInputComponent extends VaadinFillableComponent implements SingleFillable<VaadinPasswordInputComponent, String> {
    public VaadinPasswordInputComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    public void setText(String text){

    }

    public void getTest(){
        element.getText();
    }

    @Override
    public VaadinPasswordInputComponent fillWithRandom() {
        clear();
        element.sendKeys(RandomGenerator.generateRandomOnlyLetterString(), Keys.ENTER);
        return this;
    }

    @Override
    public VaadinPasswordInputComponent fillWith(String value) {
        clear();
        element.sendKeys(value, Keys.ENTER);
        return this;
    }
}

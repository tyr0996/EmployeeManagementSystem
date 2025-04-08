package hu.martin.ems.pages.core.component;

import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.SingleFillable;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class VaadinNumberInputComponent extends VaadinFillableComponent implements SingleFillable<VaadinNumberInputComponent, Number> {
    public VaadinNumberInputComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    @Override
    public VaadinNumberInputComponent fillWithRandom() {
        return fillWith(RandomGenerator.generateRandomInteger());
    }

    @Override
    public VaadinNumberInputComponent fillWith(Number value) {
        clear();
        element.sendKeys(value.toString(), Keys.ENTER);
        return this;
    }
}

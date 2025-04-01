package hu.martin.ems.pages.core.component;

import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.SingleFillable;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VaadinNumberInputComponent extends VaadinFillableComponent implements SingleFillable<VaadinNumberInputComponent, Number> {
    public VaadinNumberInputComponent(WebDriver driver, WebElement element) {
        super(driver, element);
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

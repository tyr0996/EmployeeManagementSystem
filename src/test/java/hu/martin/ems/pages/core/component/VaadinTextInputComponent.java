package hu.martin.ems.pages.core.component;

import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.SingleFillable;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VaadinTextInputComponent extends VaadinSimpleInputComponent<VaadinTextInputComponent, String> implements SingleFillable<VaadinTextInputComponent, String> {
    public VaadinTextInputComponent(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    @Override
    public VaadinTextInputComponent fillWithRandom() {
        return fillWith(RandomGenerator.generateRandomOnlyLetterString());
    }

    @Override
    public VaadinTextInputComponent fillWith(String value) {
        clear();
        element.sendKeys(value, Keys.ENTER);
        return this;
    }
}

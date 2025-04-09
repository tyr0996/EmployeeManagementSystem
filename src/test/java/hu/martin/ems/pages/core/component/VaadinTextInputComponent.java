package hu.martin.ems.pages.core.component;

import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.SingleFillable;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class VaadinTextInputComponent extends VaadinSimpleInputComponent<VaadinTextInputComponent, String> implements SingleFillable<VaadinTextInputComponent, String> {
    public VaadinTextInputComponent(WebDriver driver, By provider) {
        super(driver, provider);
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

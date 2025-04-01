package hu.martin.ems.pages.core.component;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ForgotPasswordDialog_Username extends VaadinDialogComponent {
    private static final String usernameFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field";
    private static final String nextButtonXpath = dialogXpath + "/vaadin-form-layout/vaadin-button";

    @Getter private VaadinTextInputComponent usernameField;
    @Getter private VaadinButtonComponent nextButton;

    public ForgotPasswordDialog_Username(WebDriver driver) {
        super(driver);

        initWebElements();
    }

    private void initWebElements(){
        usernameField = new VaadinTextInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(usernameFieldXpath))));
        nextButton = new VaadinButtonComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(nextButtonXpath))));
    }
}

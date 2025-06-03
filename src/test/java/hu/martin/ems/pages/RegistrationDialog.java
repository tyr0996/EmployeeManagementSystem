package hu.martin.ems.pages;

import hu.martin.ems.pages.core.component.VaadinBaseComponent;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinPasswordInputComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class RegistrationDialog extends VaadinBaseComponent {
    private static final String dialogXpath = "/html/body/vaadin-dialog-overlay";
    private static final String usernameFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field";
    private static final String passwordFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-password-field[1]";
    private static final String passwordAgainFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-password-field[2]";
    private static final String reisterButtonXpath = dialogXpath + "/vaadin-form-layout/vaadin-button";
    private static final String closeButtonXpath = dialogXpath + "/div/div/vaadin-button";

    @Getter
    private VaadinTextInputComponent usernameField;
    @Getter
    private VaadinPasswordInputComponent passwordField;
    @Getter
    private VaadinPasswordInputComponent passwordAgainField;
    @Getter
    private VaadinButtonComponent registerButton;
    @Getter
    private VaadinButtonComponent closeButton;

    public RegistrationDialog(WebDriver driver) {
        super(driver);

//        initWebElements();
    }

    public void initWebElements() {
        element = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(dialogXpath)));
        closeButton = new VaadinButtonComponent(getDriver(), By.xpath(closeButtonXpath));
        usernameField = new VaadinTextInputComponent(getDriver(), By.xpath(usernameFieldXpath));
        passwordField = new VaadinPasswordInputComponent(getDriver(), By.xpath(passwordFieldXpath));
        passwordAgainField = new VaadinPasswordInputComponent(getDriver(), By.xpath(passwordAgainFieldXpath));
        registerButton = new VaadinButtonComponent(getDriver(), By.xpath(reisterButtonXpath));
    }

    public void close() {
        closeButton.click();
    }
}

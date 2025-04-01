package hu.martin.ems.pages.core.component;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class ForgotPasswordDialog_Password extends VaadinDialogComponent {
    private static final String passwordFieldXpath = dialogXpath + "[2]/vaadin-form-layout/vaadin-password-field[1]";
    private static final String passwordAgainFieldXpath = dialogXpath + "[2]/vaadin-form-layout/vaadin-password-field[2]";
    private static final String submitButtonXpath = dialogXpath + "[2]/vaadin-form-layout/vaadin-button";

    @Getter private VaadinPasswordInputComponent passwordField;
    @Getter private VaadinPasswordInputComponent passwordAgainField;
    @Getter private VaadinButtonComponent submitButton;


    public ForgotPasswordDialog_Password(WebDriver driver) {
        super(driver);

        initWebElements();
    }

    private void initWebElements(){
        passwordField = new VaadinPasswordInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(passwordFieldXpath))));
        passwordAgainField = new VaadinPasswordInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(passwordAgainFieldXpath))));
        submitButton = new VaadinButtonComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(submitButtonXpath))));
    }
}

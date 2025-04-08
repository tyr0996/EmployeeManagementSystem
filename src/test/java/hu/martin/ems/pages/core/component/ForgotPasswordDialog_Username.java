package hu.martin.ems.pages.core.component;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

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
        usernameField = new VaadinTextInputComponent(getDriver(), By.xpath(usernameFieldXpath));
        nextButton = new VaadinButtonComponent(getDriver(), By.xpath(nextButtonXpath));
    }
}

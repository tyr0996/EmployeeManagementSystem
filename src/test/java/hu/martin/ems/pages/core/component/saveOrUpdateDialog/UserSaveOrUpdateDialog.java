package hu.martin.ems.pages.core.component.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinPasswordInputComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class UserSaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {
    private static final String usernameXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field";
    private static final String passwordXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[1]";
    private static final String passwordAgainXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[2]";
    private static final String roleXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-combo-box";

    @Getter private VaadinTextInputComponent usernameComponent;
    @Getter private VaadinPasswordInputComponent passwordComponent;
    @Getter private VaadinPasswordInputComponent passwordAgainComponent;
    @Getter private VaadinDropdownComponent roleComponent;

    public UserSaveOrUpdateDialog(WebDriver driver) {
        super(driver);
    }

    @Override
    public void initWebElements(){
        super.initWebElements();
        usernameComponent = new VaadinTextInputComponent(getDriver(), By.xpath(usernameXpath));
        passwordComponent = new VaadinPasswordInputComponent(getDriver(), By.xpath(passwordXpath));
        passwordAgainComponent = new VaadinPasswordInputComponent(getDriver(), By.xpath(passwordAgainXpath));
        roleComponent = new VaadinDropdownComponent(getDriver(), By.xpath(roleXpath));
        setAllComponents();
    }

    @Override
    public void setAllComponents(){
        this.allComponent.add(usernameComponent);
        this.allComponent.add(passwordComponent);
        this.allComponent.add(passwordAgainComponent);
        this.allComponent.add(roleComponent);
    }
}

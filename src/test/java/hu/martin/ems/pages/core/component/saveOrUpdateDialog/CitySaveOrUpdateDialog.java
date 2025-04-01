package hu.martin.ems.pages.core.component.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CitySaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {
    private static final String nameTextFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field[1]";
    private static final String countryCodeDropdownXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box";
    private static final String zipCodeTextFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field[2]";

    private VaadinTextInputComponent nameTextField;
    private VaadinDropdownComponent countryCodeDropdown;
    private VaadinTextInputComponent zipCodeTextField;

    public CitySaveOrUpdateDialog(WebDriver driver) {
        super(driver);
    }

    @Override
    public void initWebElements() {
        super.initWebElements();

        nameTextField = new VaadinTextInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(nameTextFieldXpath))));
        countryCodeDropdown = new VaadinDropdownComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(countryCodeDropdownXpath))));
        zipCodeTextField = new VaadinTextInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(zipCodeTextFieldXpath))));

        setAllComponents();
    }

    @Override
    public void setAllComponents() {
        this.allComponent.add(nameTextField);
        this.allComponent.add(countryCodeDropdown);
        this.allComponent.add(zipCodeTextField);
    }
}

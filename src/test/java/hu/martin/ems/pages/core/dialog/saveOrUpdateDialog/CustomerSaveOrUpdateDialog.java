package hu.martin.ems.pages.core.dialog.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CustomerSaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {
    private static final String firstNameFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field[1]";
    private static final String lastNameFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field[2]";
    private static final String addressComboBoxXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box";
    private static final String emailAddressXpath = dialogXpath + "/vaadin-form-layout/vaadin-email-field";

    private VaadinTextInputComponent firstNameField;
    private VaadinTextInputComponent lastNameField;
    private VaadinDropdownComponent addressComboBox;
    private VaadinTextInputComponent emailAddressField;


    public CustomerSaveOrUpdateDialog(WebDriver driver) {
        super(driver);
    }

    @Override
    public void initWebElements() {
        super.initWebElements();

        firstNameField = new VaadinTextInputComponent(getDriver(), By.xpath(firstNameFieldXpath));
        lastNameField = new VaadinTextInputComponent(getDriver(), By.xpath(lastNameFieldXpath));
        addressComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(addressComboBoxXpath));
        emailAddressField = new VaadinTextInputComponent(getDriver(), By.xpath(emailAddressXpath));

        setAllComponents();
    }

    @Override
    public void setAllComponents() {
        this.allComponent.add(firstNameField);
        this.allComponent.add(lastNameField);
        this.allComponent.add(addressComboBox);
        this.allComponent.add(emailAddressField);
    }
}

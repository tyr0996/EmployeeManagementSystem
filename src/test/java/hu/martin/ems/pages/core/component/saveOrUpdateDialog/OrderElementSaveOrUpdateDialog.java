package hu.martin.ems.pages.core.component.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinNumberInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OrderElementSaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {
    private static final String productComboBoxXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box[1]";
    private static final String unitNumberFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-number-field";
    private static final String supplierComboBoxXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box[2]";
    private static final String customerComboBoxXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box[3]";

    @Getter private VaadinDropdownComponent productComboBox;
    @Getter private VaadinNumberInputComponent unitNumberInputField;
    @Getter private VaadinDropdownComponent supplierComboBox;
    @Getter private VaadinDropdownComponent customerComboBox;


    public OrderElementSaveOrUpdateDialog(WebDriver driver) {
        super(driver);
    }

    public void initWebElements(){
        super.initWebElements();

        productComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(productComboBoxXpath));
        unitNumberInputField = new VaadinNumberInputComponent(getDriver(), By.xpath(unitNumberFieldXpath));
        supplierComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(supplierComboBoxXpath));
        customerComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(customerComboBoxXpath));

        setAllComponents();
    }

    @Override
    public void setAllComponents(){
        this.allComponent.clear();
        this.allComponent.add(productComboBox);
        this.allComponent.add(unitNumberInputField);
        this.allComponent.add(supplierComboBox);
        this.allComponent.add(customerComboBox);
    }
}

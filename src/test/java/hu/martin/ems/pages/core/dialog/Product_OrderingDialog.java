package hu.martin.ems.pages.core.dialog;

import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinDialogComponent;
import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinNumberInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Product_OrderingDialog extends VaadinDialogComponent {
    private static final String supplierComboBoxXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box";
    private static final String quantityNumberFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-number-field";
    private static final String orderButtonXpath = dialogXpath + "/vaadin-form-layout/vaadin-button";

    @Getter
    private VaadinDropdownComponent supplierComboBox;
    @Getter
    private VaadinNumberInputComponent quantity;
    @Getter
    private VaadinButtonComponent orderButton;

    public Product_OrderingDialog(WebDriver driver) {
        super(driver);
    }

    @Override
    public void initWebElements() {
        super.initWebElements();

        supplierComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(supplierComboBoxXpath));
        quantity = new VaadinNumberInputComponent(getDriver(), By.xpath(quantityNumberFieldXpath));
        orderButton = new VaadinButtonComponent(getDriver(), By.xpath(orderButtonXpath));

        setAllComponents();
    }

    @Override
    public void setAllComponents() {
        this.allComponent.clear();
        allComponent.add(supplierComboBox);
        allComponent.add(quantity);
    }
}

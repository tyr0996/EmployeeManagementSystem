package hu.martin.ems.pages.core.dialog;

import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinDialogComponent;
import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinNumberInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class Product_SellingDialog extends VaadinDialogComponent {
    private static final String customerComboBoxXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box";
    private static final String quantityNumberFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-number-field";
    private static final String sellButtonXpath = dialogXpath + "/vaadin-form-layout/vaadin-button";

    @Getter
    private VaadinDropdownComponent customerComboBox;
    @Getter
    private VaadinNumberInputComponent quantity;
    @Getter
    private VaadinButtonComponent sellButton;

    public Product_SellingDialog(WebDriver driver) {
        super(driver, By.xpath(dialogXpath));
    }

    @Override
    public void initWebElements() {
        super.initWebElements();

        customerComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(customerComboBoxXpath));
        quantity = new VaadinNumberInputComponent(getDriver(), By.xpath(quantityNumberFieldXpath));
        sellButton = new VaadinButtonComponent(getDriver(), By.xpath(sellButtonXpath));

        setAllComponents();
    }

    @Override
    public void setAllComponents() {
        allComponent.clear();
        allComponent.add(customerComboBox);
        allComponent.add(quantity);
    }


}

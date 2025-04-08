package hu.martin.ems.pages.core.component.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinNumberInputComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class ProductSaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {

    private static final String nameFieldXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field";
    private static final String buyingPriceNetXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-number-field[1]";
    private static final String buyingPriceCurrencyXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-combo-box[1]";
    private static final String sellingPriceNetXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-number-field[2]";
    private static final String sellingPriceCurrencyXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-combo-box[2]";
    private static final String amountUnitXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-combo-box[3]";
    private static final String amountXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-number-field[3]";
    private static final String taxKeyXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-combo-box[4]";

    @Getter private VaadinTextInputComponent nameComponent;
    @Getter private VaadinNumberInputComponent buyingPriceNetComponent;
    @Getter private VaadinDropdownComponent buyingPriceCurrencyComponent;
    @Getter private VaadinNumberInputComponent sellingPriceNetComponent;
    @Getter private VaadinDropdownComponent sellingPriceCurrencyComponent;
    @Getter private VaadinDropdownComponent amountUnitComponent;
    @Getter private VaadinNumberInputComponent amountComponent;
    @Getter private VaadinDropdownComponent taxKeyComponent;

    public ProductSaveOrUpdateDialog(WebDriver driver) {
        super(driver);
    }

    @Override
    public void initWebElements(){
        super.initWebElements();
        nameComponent = new VaadinTextInputComponent(getDriver(), By.xpath(nameFieldXpath));
        buyingPriceNetComponent = new VaadinNumberInputComponent(getDriver(), By.xpath(buyingPriceNetXpath));
        buyingPriceCurrencyComponent = new VaadinDropdownComponent(getDriver(), By.xpath(buyingPriceCurrencyXpath));
        sellingPriceNetComponent = new VaadinNumberInputComponent(getDriver(), By.xpath(sellingPriceNetXpath));
        sellingPriceCurrencyComponent = new VaadinDropdownComponent(getDriver(), By.xpath(sellingPriceCurrencyXpath));
        amountUnitComponent = new VaadinDropdownComponent(getDriver(), By.xpath(amountUnitXpath));
        amountComponent = new VaadinNumberInputComponent(getDriver(), By.xpath(amountXpath));
        taxKeyComponent = new VaadinDropdownComponent(getDriver(), By.xpath(taxKeyXpath));

        setAllComponents();
    }

    @Override
    public void setAllComponents(){
        this.allComponent.add(nameComponent);
        this.allComponent.add(buyingPriceNetComponent);
        this.allComponent.add(buyingPriceCurrencyComponent);
        this.allComponent.add(sellingPriceNetComponent);
        this.allComponent.add(sellingPriceCurrencyComponent);
        this.allComponent.add(amountUnitComponent);
        this.allComponent.add(amountComponent);
        this.allComponent.add(taxKeyComponent);
    }
}

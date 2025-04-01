package hu.martin.ems.pages.core.component.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AddressSaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {
    private static final String countryCodeDropdownXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box[1]";
    private static final String cityDropdownXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box[2]" ;
    private static final String streetNameTextXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field[1]";
    private static final String streetTypeDropdownXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box[3]";
    private static final String houseNumberXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field[2]";

    @Getter
    private VaadinDropdownComponent countryCodeDropdown;
    @Getter
    private VaadinDropdownComponent cityDropdown;
    @Getter
    private VaadinTextInputComponent streetNameTextInput;
    @Getter
    private VaadinDropdownComponent streetTypeDropdown;
    @Getter
    private VaadinTextInputComponent houseNumberInput;


    public AddressSaveOrUpdateDialog(WebDriver driver) {
        super(driver);
//        initWebElements();
    }
    //*[@id="vaadin-combo-box-item-1"]
    public void initWebElements() {
        super.initWebElements();

        countryCodeDropdown = new VaadinDropdownComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(countryCodeDropdownXpath))));
        cityDropdown = new VaadinDropdownComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(cityDropdownXpath))));
        streetNameTextInput = new VaadinTextInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(streetNameTextXpath))));
        streetTypeDropdown = new VaadinDropdownComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(streetTypeDropdownXpath))));
        houseNumberInput = new VaadinTextInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(houseNumberXpath))));

        setAllComponents();
    }

    @Override
    public void setAllComponents() {
        allComponent.add(countryCodeDropdown);
        allComponent.add(cityDropdown);
        allComponent.add(streetNameTextInput);
        allComponent.add(streetTypeDropdown);
        allComponent.add(houseNumberInput);
    }
}

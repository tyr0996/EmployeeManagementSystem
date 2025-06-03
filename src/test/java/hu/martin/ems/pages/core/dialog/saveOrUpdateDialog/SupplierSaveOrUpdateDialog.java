package hu.martin.ems.pages.core.dialog.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SupplierSaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {
    private static final String nameFieldXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field";
    private static final String addressXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-combo-box[1]";

    @Getter
    private VaadinTextInputComponent nameComponent;
    @Getter
    private VaadinDropdownComponent addressComponent;

    public SupplierSaveOrUpdateDialog(WebDriver driver) {
        super(driver);
    }

    @Override
    public void initWebElements() {
        super.initWebElements();
        nameComponent = new VaadinTextInputComponent(getDriver(), By.xpath(nameFieldXpath));
        addressComponent = new VaadinDropdownComponent(getDriver(), By.xpath(addressXpath));

        setAllComponents();
    }

    @Override
    public void setAllComponents() {
        this.allComponent.add(nameComponent);
        this.allComponent.add(addressComponent);
    }
}

package hu.martin.ems.pages.core.dialog.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinMultipleSelectDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class PermissionSaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {

    private static final String nameTextFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field";

    private static final String rolesComboBoxXpath = dialogXpath + "/vaadin-form-layout/vaadin-multi-select-combo-box";

    @Getter
    private VaadinTextInputComponent nameField;
    @Getter
    private VaadinMultipleSelectDropdownComponent rolesComboBox;

    public PermissionSaveOrUpdateDialog(WebDriver driver) {
        super(driver);
    }

    public void initWebElements() {
        super.initWebElements();

        nameField = new VaadinTextInputComponent(getDriver(), By.xpath(nameTextFieldXpath));
        rolesComboBox = new VaadinMultipleSelectDropdownComponent(getDriver(), By.xpath(rolesComboBoxXpath));

        setAllComponents();
    }

    @Override
    public void setAllComponents() {
        this.allComponent.clear();
        this.allComponent.add(nameField);
        this.allComponent.add(rolesComboBox);
    }
}

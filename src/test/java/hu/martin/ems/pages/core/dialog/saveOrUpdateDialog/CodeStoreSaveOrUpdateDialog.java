package hu.martin.ems.pages.core.dialog.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CodeStoreSaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {

    private static final String nameTextFieldXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field[1]";
    private static final String parentDropdownXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box";
    private static final String deletableCheckboxXpath = dialogXpath + "/vaadin-form-layout/vaadin-checkbox"; //TODO

    private VaadinTextInputComponent nameTextField;
    private VaadinDropdownComponent parentDropdown;
    private VaadinCheckboxComponent deletableCheckbox;

    public CodeStoreSaveOrUpdateDialog(WebDriver driver){
        super(driver);
    }

    @Override
    public void initWebElements(){
        super.initWebElements();
        nameTextField = new VaadinTextInputComponent(getDriver(), By.xpath(nameTextFieldXpath));
        parentDropdown = new VaadinDropdownComponent(getDriver(), By.xpath(parentDropdownXpath));
        deletableCheckbox = new VaadinCheckboxComponent(getDriver(), By.xpath(deletableCheckboxXpath));

        setAllComponents();
    }

    @Override
    public void setAllComponents(){
        this.allComponent.clear();

        this.allComponent.add(nameTextField);
        this.allComponent.add(parentDropdown);
        this.allComponent.add(deletableCheckbox);
    }

}

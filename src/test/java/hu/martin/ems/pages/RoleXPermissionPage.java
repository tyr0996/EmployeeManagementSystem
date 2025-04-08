package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinMultipleSelectDropdownComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class RoleXPermissionPage extends EmptyLoggedInVaadinPage implements ILoggedInPage {
    private static final String roleComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout/vaadin-combo-box";
    private static final String permissionsComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout/vaadin-multi-select-combo-box";
    private static final String saveButtonXpath = contentLayoutXpath + "/vaadin-form-layout/vaadin-button";

    @Getter private VaadinDropdownComponent roleComboBox;
    @Getter private VaadinMultipleSelectDropdownComponent permissionsComboBox;
    @Getter private VaadinButtonComponent saveButton;

    public RoleXPermissionPage(WebDriver driver, int port) {
        super(driver, port);
        initWebElements();
    }

    @Override
    public RoleXPermissionPage initWebElements() {
        roleComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(roleComboBoxXpath));
        permissionsComboBox = new VaadinMultipleSelectDropdownComponent(getDriver(), By.xpath(permissionsComboBoxXpath));
        saveButton = new VaadinButtonComponent(getDriver(), By.xpath(saveButtonXpath));
        return this;
    }
}

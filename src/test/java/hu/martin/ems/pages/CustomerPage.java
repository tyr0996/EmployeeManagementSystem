package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.CustomerSaveOrUpdateDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CustomerPage extends SimpleVaadinGridPage<CustomerPage> implements ISimpleVaadinGridPage<CustomerPage> {
    public CustomerPage(WebDriver driver, int port) {
        super(driver, port, "Customer", null);
        initWebElements();
    }

    @Override
    public CustomerPage initWebElements(){

        this.showDeletedCheckBox = new VaadinCheckboxComponent(getDriver(), By.xpath(showDeletedCheckBoxXpath));
        this.createButton = new VaadinButtonComponent(getDriver(), By.xpath(createButtonXpath));
        this.grid = new VaadinGridComponent(getDriver(), By.xpath(gridXpath));
        this.saveOrUpdateDialog = new CustomerSaveOrUpdateDialog(getDriver());

        return this;
    }
}

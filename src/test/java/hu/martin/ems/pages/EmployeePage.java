package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinSwitchComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.EmployeeSaveOrUpdateDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class EmployeePage extends SimpleVaadinGridPage<EmployeePage> implements ISimpleVaadinGridPage<EmployeePage> {


    public EmployeePage(WebDriver driver, int port) {
        super(driver, port, "Employee", null);
        initWebElements();
    }

    @Override
    public EmployeePage initWebElements() {

        this.showDeletedSwitch = new VaadinSwitchComponent(getDriver(), By.xpath(showDeletedSwitchXpath));
        this.createButton = new VaadinButtonComponent(getDriver(), By.xpath(createButtonXpath));
        this.grid = new VaadinGridComponent(getDriver(), By.xpath(gridXpath));
        this.saveOrUpdateDialog = new EmployeeSaveOrUpdateDialog(getDriver());

        return this;
    }
}

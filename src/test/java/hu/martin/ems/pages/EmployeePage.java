package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.EmployeeSaveOrUpdateDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class EmployeePage extends SimpleVaadinGridPage<EmployeePage> implements ISimpleVaadinGridPage<EmployeePage> {


    public EmployeePage(WebDriver driver, int port) {
        super(driver, port, "Employee", null);
        initWebElements();
    }

    @Override
    public EmployeePage initWebElements(){

        this.showDeletedCheckBox = new VaadinCheckboxComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(showDeletedCheckBoxXpath))));
        this.createButton = new VaadinButtonComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(createButtonXpath))));
        this.grid = new VaadinGridComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(gridXpath))));
        this.saveOrUpdateDialog = new EmployeeSaveOrUpdateDialog(getDriver());

        return this;
    }
}

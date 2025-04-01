package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.OrderElementSaveOrUpdateDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OrderElementPage extends SimpleVaadinGridPage<OrderElementPage> implements ISimpleVaadinGridPage<OrderElementPage> {

    public OrderElementPage(WebDriver driver, int port) {
        super(driver, port, "OrderElement", null);
        initWebElements();
    }

    @Override
    public OrderElementPage initWebElements(){
        this.showDeletedCheckBox = new VaadinCheckboxComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(showDeletedCheckBoxXpath))));
        this.createButton = new VaadinButtonComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(createButtonXpath))));
        this.grid = new VaadinGridComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(gridXpath))));
        this.saveOrUpdateDialog = new OrderElementSaveOrUpdateDialog(getDriver());

        return this;
    }
}

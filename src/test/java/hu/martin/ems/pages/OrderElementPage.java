package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinSwitchComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.OrderElementSaveOrUpdateDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class OrderElementPage extends SimpleVaadinGridPage<OrderElementPage> implements ISimpleVaadinGridPage<OrderElementPage> {

    public OrderElementPage(WebDriver driver, int port) {
        super(driver, port, "OrderElement", null);
        initWebElements();
    }

    @Override
    public OrderElementPage initWebElements() {
        this.showDeletedSwitch = new VaadinSwitchComponent(getDriver(), By.xpath(showDeletedSwitchXpath));
        this.createButton = new VaadinButtonComponent(getDriver(), By.xpath(createButtonXpath));
        this.grid = new VaadinGridComponent(getDriver(), By.xpath(gridXpath));
        this.saveOrUpdateDialog = new OrderElementSaveOrUpdateDialog(getDriver());

        return this;
    }
}

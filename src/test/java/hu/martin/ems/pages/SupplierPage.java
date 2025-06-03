package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.SupplierSaveOrUpdateDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SupplierPage extends SimpleVaadinGridPage<SupplierPage> implements ISimpleVaadinGridPage<SupplierPage> {
    public SupplierPage(WebDriver driver, int port) {
        super(driver, port, "Supplier", null);
        initWebElements();
    }

    @Override
    public SupplierPage initWebElements() {
        this.showDeletedCheckBox = new VaadinCheckboxComponent(getDriver(), By.xpath(showDeletedCheckBoxXpath));
        this.createButton = new VaadinButtonComponent(getDriver(), By.xpath(createButtonXpath));
        this.grid = new VaadinGridComponent(getDriver(), By.xpath(gridXpath));
        this.saveOrUpdateDialog = new SupplierSaveOrUpdateDialog(getDriver());

        return this;
    }
}

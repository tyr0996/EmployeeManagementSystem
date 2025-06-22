package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinSwitchComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.PermissionSaveOrUpdateDialog;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class PermissionPage extends SimpleVaadinGridPage<PermissionPage> implements ISimpleVaadinGridPage<PermissionPage> {
    public PermissionPage(WebDriver driver, int port) {
        super(driver, port, "Permission", null);
        initWebElements();
    }

    private static final String showDeletedSwitchXpath = contentLayoutXpath + "/vaadin-horizontal-layout[2]/switch";
    private static final String createButtonXpath = contentLayoutXpath + "/vaadin-horizontal-layout[2]/vaadin-button";
    private static final String gridXpath = contentLayoutXpath + "/vaadin-grid";

    @Getter
    private AccessManagementHeader header;

    @Override
    public PermissionPage initWebElements() {
        this.showDeletedSwitch = new VaadinSwitchComponent(getDriver(), By.xpath(showDeletedSwitchXpath));
        this.createButton = new VaadinButtonComponent(getDriver(), By.xpath(createButtonXpath));
        this.grid = new VaadinGridComponent(getDriver(), By.xpath(gridXpath));
        this.saveOrUpdateDialog = new PermissionSaveOrUpdateDialog(getDriver());
        this.header = new AccessManagementHeader(getDriver(), getPort());

        return this;
    }
}

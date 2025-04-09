package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.RoleSaveOrUpdateDialog;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RolePage extends SimpleVaadinGridPage<RolePage> implements ISimpleVaadinGridPage<RolePage> {
    public RolePage(WebDriver driver, int port) {
        super(driver, port, "Role", null);

        initWebElements();
    }

    private static final String showDeletedCheckBoxXpath = contentLayoutXpath + "/vaadin-horizontal-layout[2]/vaadin-checkbox";
    private static final String createButtonXpath = contentLayoutXpath + "/vaadin-horizontal-layout[2]/vaadin-button";
    private static final String gridXpath = contentLayoutXpath + "/vaadin-grid";

    @Getter private AccessManagementHeader header;

    @Override
    public RolePage initWebElements() {
        this.showDeletedCheckBox = new VaadinCheckboxComponent(getDriver(), By.xpath(showDeletedCheckBoxXpath));
        this.createButton = new VaadinButtonComponent(getDriver(), By.xpath(createButtonXpath));
        this.grid = new VaadinGridComponent(getDriver(), By.xpath(gridXpath));
        this.saveOrUpdateDialog = new RoleSaveOrUpdateDialog(getDriver());
        this.header = new AccessManagementHeader(getDriver(), getPort());

        return this;
    }
}

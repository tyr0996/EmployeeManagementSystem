package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.component.VaadinSwitchComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.CodeStoreSaveOrUpdateDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CodeStorePage extends SimpleVaadinGridPage<CodeStorePage> implements ISimpleVaadinGridPage<CodeStorePage> {

    private static final String showOnlyDeletableXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/switch";

    public CodeStorePage(WebDriver driver, int port) {
        super(driver, port, "CodeStore", showOnlyDeletableXpath);
        initWebElements();
    }

    @Override
    public CodeStorePage initWebElements() {
        this.showDeletedSwitch = new VaadinSwitchComponent(getDriver(), By.xpath(showDeletedSwitchXpath));
        this.createButton = new VaadinButtonComponent(getDriver(), By.xpath(createButtonXpath));
        this.grid = new VaadinGridComponent(getDriver(), By.xpath(gridXpath));
        this.saveOrUpdateDialog = new CodeStoreSaveOrUpdateDialog(getDriver());

        return this;
    }

}

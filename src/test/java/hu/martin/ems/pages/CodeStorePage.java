package hu.martin.ems.pages;

import hu.martin.ems.pages.core.ISimpleVaadinGridPage;
import hu.martin.ems.pages.core.SimpleVaadinGridPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.CodeStoreSaveOrUpdateDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CodeStorePage extends SimpleVaadinGridPage<CodeStorePage> implements ISimpleVaadinGridPage<CodeStorePage> {

    private static final String showOnlyDeletableXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-checkbox";

    public CodeStorePage(WebDriver driver, int port){
        super(driver, port, "CodeStore", showOnlyDeletableXpath);
        initWebElements();
    }

    @Override
    public CodeStorePage initWebElements(){
        this.showDeletedCheckBox = new VaadinCheckboxComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(showDeletedCheckBoxXpath))));
        this.createButton = new VaadinButtonComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(createButtonXpath))));
        this.grid = new VaadinGridComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(gridXpath))));
        this.saveOrUpdateDialog = new CodeStoreSaveOrUpdateDialog(getDriver());

        return this;
    }

}

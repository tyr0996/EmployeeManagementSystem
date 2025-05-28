package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinParagraphComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdminToolsPage extends EmptyLoggedInVaadinPage implements ILoggedInPage {

    private static final String clearDatabaseButtonXpath = contentLayoutXpath + "/vaadin-button";
    private static final String exportApisButtonXpath = contentLayoutXpath + "/a/vaadin-button";
    private static final String healthStatusXpath = contentLayoutXpath + "/div/vaadin-horizontal-layout/p[2]";
    @Getter
    private VaadinButtonComponent clearDatabaseButton;
    @Getter
    private VaadinButtonComponent exportApisButton;
    @Getter
    private VaadinParagraphComponent healthStatus;

    public AdminToolsPage(WebDriver driver, int port){
        super(driver, port);
        initWebElements();
    }

    @Override
    public AdminToolsPage initWebElements(){
        clearDatabaseButton = new VaadinButtonComponent(getDriver(), By.xpath(clearDatabaseButtonXpath));
        exportApisButton = new VaadinButtonComponent(getDriver(), By.xpath(exportApisButtonXpath));
        healthStatus = new VaadinParagraphComponent(getDriver(), By.xpath(healthStatusXpath));

        return this;
    }
}

package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AdminToolsPage extends EmptyLoggedInVaadinPage implements ILoggedInPage {

    private static final String clearDatabaseButtonXpath = contentLayoutXpath + "/vaadin-button";
    private static final String exportApisButtonXpath = contentLayoutXpath + "/a/vaadin-button";
    @Getter
    private VaadinButtonComponent clearDatabaseButton;
    @Getter
    private VaadinButtonComponent exportApisButton;

    public AdminToolsPage(WebDriver driver, int port){
        super(driver, port);
        initWebElements();
    }

    @Override
    public AdminToolsPage initWebElements(){
        clearDatabaseButton = new VaadinButtonComponent(getDriver(), By.xpath(clearDatabaseButtonXpath));
        exportApisButton = new VaadinButtonComponent(getDriver(), By.xpath(exportApisButtonXpath));
        return this;
    }
}

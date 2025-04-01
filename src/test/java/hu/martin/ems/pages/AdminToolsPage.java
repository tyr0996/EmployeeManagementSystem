package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AdminToolsPage extends EmptyLoggedInVaadinPage implements ILoggedInPage {

    private static final String clearDatabaseButtonXpath = contentLayoutXpath + "/vaadin-button";
    @Getter
    private WebElement clearDatabaseButton;

    public AdminToolsPage(WebDriver driver, int port){
        super(driver, port);
        initWebElements();
    }

    @Override
    public AdminToolsPage initWebElements(){
        clearDatabaseButton = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(clearDatabaseButtonXpath)));
        return this;
    }
}

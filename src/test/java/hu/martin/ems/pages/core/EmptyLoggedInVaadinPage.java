package hu.martin.ems.pages.core;

import hu.martin.ems.pages.core.component.IVaadinBaseComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class EmptyLoggedInVaadinPage extends VaadinPage implements ILoggedInPage{
    @Getter protected SideMenu sideMenu;

    public EmptyLoggedInVaadinPage(WebDriver driver, int port) {
        super(driver, port);
        initWebElements();
    }

    @Override
    public EmptyLoggedInVaadinPage initWebElements(){
        this.sideMenu = new SideMenu(getDriver(), By.xpath(sideMenuXpath));
        return this;
    }


    public void logout() {
        if(!getDriver().getCurrentUrl().contains("http://localhost:" + getPort() + "/login") &&
                !getDriver().getCurrentUrl().contains("data:,")){
            getSideMenu().logout();
        }
    }
}

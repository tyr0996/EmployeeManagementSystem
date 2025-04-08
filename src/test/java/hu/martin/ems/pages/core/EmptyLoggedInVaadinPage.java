package hu.martin.ems.pages.core;

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
            WebElement logoutButton = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout/vaadin-button")));

            JavascriptExecutor js = (JavascriptExecutor) getDriver();
            js.executeScript("arguments[0].click()", logoutButton);
            getWait().until(ExpectedConditions.urlContains("http://localhost:" + getPort() + "/login"));
            getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"input-vaadin-text-field-6\"]")));
        }
    }

}

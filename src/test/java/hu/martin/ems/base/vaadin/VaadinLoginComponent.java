package hu.martin.ems.base.vaadin;

import hu.martin.ems.base.GridTestingUtil;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VaadinLoginComponent {
    private WebDriver driver;
    private int port;

    private GridTestingUtil gridTestingUtil;

    public VaadinLoginComponent(WebDriver driver, int port, GridTestingUtil gridTestingUtil){
        this.gridTestingUtil = gridTestingUtil;
        this.driver = driver;
        this.port = port;
    }

    public void logout() throws InterruptedException {
        if(!driver.getCurrentUrl().contains("http://localhost:" + port + "/login") &&
                !driver.getCurrentUrl().contains("data:,")){
            WebElement logoutButton = gridTestingUtil.findVisibleElementWithXpath("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout/vaadin-button");
            JavascriptExecutor js = (JavascriptExecutor) driver;
//            logoutButton.click();
            js.executeScript("arguments[0].click()", logoutButton);
            Thread.sleep(100);
            System.out.println("Logout utáni: " + driver.getCurrentUrl());
            assert driver.getCurrentUrl().contains("http://localhost:" + port + "/login");
        }
    }

}

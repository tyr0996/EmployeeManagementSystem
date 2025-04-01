package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class Page403 extends EmptyLoggedInVaadinPage implements ILoggedInPage {

    private static final String angryCatPictureXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/img";
    private static final String permissionMessageXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/div[1]";
    private static final String tipTextXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/div[2]";
    private static final String coffeeTextXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/div[3]";


    @Getter
    private WebElement angryCat;
    @Getter
    private WebElement permissionMessage;
    @Getter
    private WebElement tipText;
    @Getter
    private WebElement coffeeText;

    public Page403(WebDriver driver, int port) {
        super(driver, port);
        initWebElements();
    }

    @Override
    public Page403 initWebElements() {
        angryCat = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(angryCatPictureXpath)));
        permissionMessage = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(permissionMessageXpath)));
        tipText = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(tipTextXpath)));
        coffeeText = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(coffeeTextXpath)));

        return this;
    }
}

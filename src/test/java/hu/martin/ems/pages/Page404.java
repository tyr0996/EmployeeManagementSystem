package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class Page404 extends EmptyLoggedInVaadinPage implements ILoggedInPage {

    private static final String playfulCatPictureXpath = contentLayoutXpath + "/img";
    private static final String notFoundMessageXpath = contentLayoutXpath + "/div[1]";
    private static final String tipTextXpath = contentLayoutXpath + "/div[2]";


    @Getter
    private WebElement playfulCat;
    @Getter
    private WebElement notFoundMessage;
    @Getter
    private WebElement tipText;

    public Page404(WebDriver driver, int port) {
        super(driver, port);
        initWebElements();
    }

    @Override
    public Page404 initWebElements() {
        playfulCat = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(playfulCatPictureXpath)));
        notFoundMessage = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(notFoundMessageXpath)));
        tipText = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(tipTextXpath)));

        return this;
    }


}

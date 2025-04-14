package hu.martin.ems.pages;

import hu.martin.ems.pages.core.component.VaadinBaseComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class InternalErrorNotification extends VaadinBaseComponent {
    private static final String captionXpath = "./div[1]";
    private static final String messageXpath = "./div[2]";

    @Getter private WebElement caption;
    @Getter private WebElement message;

    public InternalErrorNotification(WebDriver driver){
        super(driver, By.xpath("/html/body/div[3]"));
        caption = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(this.element, By.xpath(captionXpath))).get(0);
        message = getWait().until(ExpectedConditions.visibilityOfNestedElementsLocatedBy(this.element, By.xpath(messageXpath))).get(0);
    }
}

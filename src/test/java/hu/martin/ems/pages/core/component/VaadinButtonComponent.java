package hu.martin.ems.pages.core.component;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class VaadinButtonComponent extends VaadinBaseComponent {
    public VaadinButtonComponent(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public VaadinButtonComponent(WebDriver driver, String elementXpath){
        super(driver, elementXpath);
    }

    public void click(){
        element.click();
    }
}

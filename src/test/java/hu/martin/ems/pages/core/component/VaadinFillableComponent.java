package hu.martin.ems.pages.core.component;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class VaadinFillableComponent extends VaadinBaseComponent implements Fillable {

    public VaadinFillableComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    public VaadinFillableComponent(WebDriver driver, WebElement scope, By provider, int index) {
        super(driver, scope, provider, index);
    }

    public String getTitle(){
        return element.findElement(By.tagName("label")).getText();
    }


    public String getErrorMessage(){
        printToConsole(element.findElement(By.tagName("div")));
        return element.findElement(By.tagName("div")).getText();
    }

    public void clear(){
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
    }
}

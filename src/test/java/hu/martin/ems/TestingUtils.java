package hu.martin.ems;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class TestingUtils {
    public static WebElement getParent(WebElement element){
        return element.findElement(By.xpath("./.."));
    }

}

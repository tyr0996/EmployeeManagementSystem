package hu.martin.ems.pages.core.component;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class VaadinParagraphComponent extends VaadinBaseComponent {
    public VaadinParagraphComponent(WebDriver driver, By provider){
        super(driver, provider);
    }

    public String getText(){
        return this.element.getText();
    }
}

package hu.martin.ems.pages.core.component;

import hu.martin.ems.base.selenium.WebDriverProvider;
import org.openqa.selenium.WebDriver;

public interface IVaadinBaseComponent {
    WebDriver driver = WebDriverProvider.get();

    default WebDriver getDriver(){
        return driver;
    }
}

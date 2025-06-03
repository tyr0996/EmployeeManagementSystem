package hu.martin.ems.pages.core.component;

import hu.martin.ems.base.selenium.WebDriverProvider;
import hu.martin.ems.core.config.BeanProvider;
import org.openqa.selenium.WebDriver;

public interface IVaadinBaseComponent {
    WebDriver driver = BeanProvider.getBean(WebDriverProvider.class).get();

    default WebDriver getDriver() {
        return driver;
    }
}

package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinDatePickerComponent;
import hu.martin.ems.pages.core.component.VaadinGridComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CurrencyPage extends EmptyLoggedInVaadinPage implements ILoggedInPage {


    private static final String datePickerXPath = contentLayoutXpath + "/vaadin-date-picker/input";

    private static final String gridXPath = contentLayoutXpath + "/vaadin-grid";

    private static final String fetchButtonXpath = contentLayoutXpath + "/vaadin-button";

    @Getter
    private VaadinDatePickerComponent datePicker;
    @Getter
    private VaadinGridComponent grid;
    @Getter
    private VaadinButtonComponent fetchButton;

    public CurrencyPage(WebDriver driver, int port) {
        super(driver, port);

        initWebElements();
    }

    @Override
    public CurrencyPage initWebElements() {
        grid = new VaadinGridComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(gridXPath))));
        datePicker = new VaadinDatePickerComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(datePickerXPath))));
        fetchButton = new VaadinButtonComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(fetchButtonXpath))));

        return this;
    }
}

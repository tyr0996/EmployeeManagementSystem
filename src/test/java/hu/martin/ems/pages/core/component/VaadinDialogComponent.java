package hu.martin.ems.pages.core.component;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

public class VaadinDialogComponent extends VaadinBaseComponent {

    @Getter
    protected VaadinButtonComponent closeButton;
    @Getter
    protected VaadinButtonComponent saveButton;


    @Getter
    protected List<Fillable> allComponent = new ArrayList<>();

    protected static final String dialogXpath = "/html/body/vaadin-dialog-overlay";
    private static final String closeButtonXpath = dialogXpath + "/div/div/vaadin-button";

    public VaadinDialogComponent(WebDriver driver, By provider){
        super(driver, provider);
    }


//    public VaadinDialogComponent(WebDriver driver, WebElement element) {
//        super(driver, element);
//    }
//
//    public VaadinDialogComponent(WebDriver driver, String elementXpath) {
//        super(driver, elementXpath);
//    }

    public VaadinDialogComponent(WebDriver driver) {
        super(driver);
    }

    public void close(){
        getCloseButton().click();
        getWait().until(ExpectedConditions.invisibilityOf(element));
    }
}

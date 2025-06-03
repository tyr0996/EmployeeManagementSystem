package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class AccessManagementHeader extends EmptyLoggedInVaadinPage implements ILoggedInPage {
    private static final String roleButtonXpath = contentLayoutXpath + "/vaadin-horizontal-layout/vaadin-button[1]";
    private static final String permissionButtonXpath = contentLayoutXpath + "/vaadin-horizontal-layout/vaadin-button[2]";
    private static final String roleXPermissionButtonXpath = contentLayoutXpath + "/vaadin-horizontal-layout/vaadin-button[3]";

    @Getter
    private VaadinButtonComponent roleButton;
    @Getter
    private VaadinButtonComponent permissionButton;
    @Getter
    private VaadinButtonComponent roleXPermissionButton;


    public AccessManagementHeader(WebDriver driver, int port) {
        super(driver, port);

        initWebElements();
    }


    @Override
    public AccessManagementHeader initWebElements() {
        roleButton = new VaadinButtonComponent(getDriver(), By.xpath(roleButtonXpath));
        permissionButton = new VaadinButtonComponent(getDriver(), By.xpath(permissionButtonXpath));
        roleXPermissionButton = new VaadinButtonComponent(getDriver(), By.xpath(roleXPermissionButtonXpath));

        return this;
    }
}

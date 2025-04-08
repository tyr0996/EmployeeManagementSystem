package hu.martin.ems.pages.core;

import hu.martin.ems.pages.core.component.VaadinBaseComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SideMenu extends VaadinBaseComponent {
    private static final String SIDE_MENU = "//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout";

    public static final String ADMIN_MENU = SIDE_MENU + "/span[1]";
    public static final String ORDERS_MENU = SIDE_MENU + "/span[2]";
    public static final String EMPLOYEE_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[1]/span";
    public static final String ACESS_MANAGEMENT_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[2]/span";
    public static final String CODESTORE_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[3]/span";
    public static final String CITY_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[4]/span";
    public static final String ADDRESS_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[5]/span";
    public static final String CUSTOMER_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[6]/span";
    public static final String PRODUCT_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[7]/span";
    public static final String SUPPLIER_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[8]/span";
    public static final String CURRENCY_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[9]/span";
    public static final String USER_SUB_MENU = SIDE_MENU + "/vaadin-horizontal-layout[10]/span";
    public static final String ADMINTOOLS_SUB_MENU = SIDE_MENU + "/vaadin-horizontal-layout[11]/span";
    public static final String ORDER_ELEMENT_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[12]/span";
    public static final String ORDER_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[13]/span";
    public static final String ORDER_CREATE_SUBMENU = SIDE_MENU + "/vaadin-horizontal-layout[14]/span";

    @Getter protected WebElement adminMenu;
    @Getter protected WebElement ordersMenu;

    private WebElement element;
    public SideMenu(WebDriver driver, By provider){
        super(driver, provider);
        this.element = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(SIDE_MENU)));
        this.adminMenu = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(ADMIN_MENU)));
        this.ordersMenu = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(ORDERS_MENU)));
    }

    public void navigate(String mainMenu, String subMenu){
        getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(mainMenu))).click();
        getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(subMenu))).click();
        getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(mainMenu))).click();
    }
}

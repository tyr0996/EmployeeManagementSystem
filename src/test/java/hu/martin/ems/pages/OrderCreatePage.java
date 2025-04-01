package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.*;
import hu.martin.ems.pages.core.performResult.PerformCreateResult;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

public class OrderCreatePage extends EmptyLoggedInVaadinPage implements ILoggedInPage {
    private static final String multipleSelectGridXpath = contentLayoutXpath + "/vaadin-grid";
    private static final String customerComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout[1]/vaadin-combo-box";
    private static final String showPreviouslyOrderedElementsCheckboxXpath = contentLayoutXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String currencyComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout[2]/vaadin-combo-box[1]";
    private static final String paymentTypeComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout[2]/vaadin-combo-box[2]";
    private static final String createOrderButtonXpath = contentLayoutXpath + "/vaadin-form-layout[2]/vaadin-button";

    @Getter private VaadinMultipleSelectGridComponent grid;
    @Getter private VaadinDropdownComponent customerComboBox;
    @Getter private VaadinCheckboxComponent showPreviouslyOrderedElementsCheckBox;
    @Getter private VaadinDropdownComponent currencyComboBox;
    @Getter private VaadinDropdownComponent paymentTypeComboBox;
    @Getter private VaadinButtonComponent createOrderButton;

    public OrderCreatePage(WebDriver driver, int port) {
        super(driver, port);

        initWebElements();
    }

    public PerformCreateResult performCreate(String customerName) throws NoSuchElementException {
        for(int i = 0; i < customerComboBox.getElementNumber(); i++){
            if(customerName != null){
                customerComboBox.fillWith(customerName);
            }
            if(grid.getTotalRowNumber() == 0){
                getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
                OrderElementPage oePage = new OrderElementPage(getDriver(), getPort());
                LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
                withData.put("Customer", customerComboBox.getSelectedElement());
                oePage.performCreate(withData);
//                new VaadinNotificationComponent(getDriver()).close();
                getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
                this.initWebElements();
                performCreate(withData.get("Customer").toString());
            }
            if(grid.getTotalRowNumber() != 0){
                grid.selectElements(1);
                currencyComboBox.fillWith(0);
                paymentTypeComboBox.fillWith(0);
                createOrderButton.click();
                return new PerformCreateResult();
            }
        }
        throw new NoSuchElementException("There wasn't any order element, which not ordered");
    }

    @Override
    public OrderCreatePage initWebElements() {
        grid = new VaadinMultipleSelectGridComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(multipleSelectGridXpath))));
        customerComboBox = new VaadinDropdownComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(customerComboBoxXpath))));
        showPreviouslyOrderedElementsCheckBox = new VaadinCheckboxComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(showPreviouslyOrderedElementsCheckboxXpath))));
        currencyComboBox = new VaadinDropdownComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(currencyComboBoxXpath))));
        paymentTypeComboBox = new VaadinDropdownComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(paymentTypeComboBoxXpath))));
        createOrderButton = new VaadinButtonComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(createOrderButtonXpath))));
        sideMenu = new SideMenu(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(sideMenuXpath))));
        return this;
    }
}

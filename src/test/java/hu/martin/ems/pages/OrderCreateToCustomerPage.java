package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.ILoggedInPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinCheckboxComponent;
import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinMultipleSelectGridComponent;
import hu.martin.ems.pages.core.performResult.PerformCreateResult;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

public class OrderCreateToCustomerPage extends EmptyLoggedInVaadinPage implements ILoggedInPage {
    private static final String multipleSelectGridXpath = contentLayoutXpath + "/vaadin-grid";
    private static final String customerComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout[1]/vaadin-combo-box";
    private static final String showPreviouslyOrderedElementsCheckboxXpath = contentLayoutXpath + "/vaadin-form-layout[1]/vaadin-checkbox";
    private static final String currencyComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout[2]/vaadin-combo-box[1]";
    private static final String paymentTypeComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout[2]/vaadin-combo-box[2]";
    private static final String createOrderButtonXpath = contentLayoutXpath + "/vaadin-form-layout[2]/vaadin-button";

    @Getter
    private VaadinMultipleSelectGridComponent grid;
    @Getter
    private VaadinDropdownComponent customerComboBox;
    @Getter
    private VaadinCheckboxComponent showPreviouslyOrderedElementsCheckBox;
    @Getter
    private VaadinDropdownComponent currencyComboBox;
    @Getter
    private VaadinDropdownComponent paymentTypeComboBox;
    @Getter
    private VaadinButtonComponent createOrderButton;

    public OrderCreateToCustomerPage(WebDriver driver, int port) {
        super(driver, port);

        initWebElements();
    }

    public PerformCreateResult performCreate(String customerName) throws NoSuchElementException {
        for (int i = 0; i < customerComboBox.getElementNumber(); i++) {
            if (customerName == null) {
                customerComboBox.fillWithRandom();
            }
            if (customerName != null) {
                customerComboBox.fillWith(customerName);
            }
            grid.waitForRefresh();
//            System.out.println("i: " + i + "     totalRowNumber: " + grid.getTotalRowNumber());
            int totalRow = grid.getTotalRowNumber();
            if (totalRow == 0) { //TODO kicserélni a grid-esre.
                LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
                withData.put("Customer", customerComboBox.getSelectedElement());
                getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
                OrderElementPage oePage = new OrderElementPage(getDriver(), getPort());
                oePage.performCreate(withData);
//                new VaadinNotificationComponent(getDriver()).close();
                getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
                this.initWebElements();
                return performCreate(withData.get("Customer").toString());
            } else {
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
    public OrderCreateToCustomerPage initWebElements() {
        grid = new VaadinMultipleSelectGridComponent(getDriver(), By.xpath(multipleSelectGridXpath));
        customerComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(customerComboBoxXpath));
        showPreviouslyOrderedElementsCheckBox = new VaadinCheckboxComponent(getDriver(), By.xpath(showPreviouslyOrderedElementsCheckboxXpath));
        currencyComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(currencyComboBoxXpath));
        paymentTypeComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(paymentTypeComboBoxXpath));
        createOrderButton = new VaadinButtonComponent(getDriver(), By.xpath(createOrderButtonXpath));
        sideMenu = new SideMenu(getDriver(), By.xpath(sideMenuXpath));
        return this;
    }
}

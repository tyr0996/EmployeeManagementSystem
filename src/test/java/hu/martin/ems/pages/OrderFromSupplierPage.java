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

public class OrderFromSupplierPage extends EmptyLoggedInVaadinPage implements ILoggedInPage {
    private static final String multipleSelectGridXpath = contentLayoutXpath + "/vaadin-grid";
    private static final String supplierComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout[1]/vaadin-combo-box";
    private static final String showPreviouslyOrderedElementsCheckboxXpath = contentLayoutXpath + "/vaadin-form-layout[1]/vaadin-checkbox";
    private static final String currencyComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout[2]/vaadin-combo-box[1]";
    private static final String paymentTypeComboBoxXpath = contentLayoutXpath + "/vaadin-form-layout[2]/vaadin-combo-box[2]";
    private static final String createOrderButtonXpath = contentLayoutXpath + "/vaadin-form-layout[2]/vaadin-button";

    @Getter
    private VaadinMultipleSelectGridComponent grid;
    @Getter
    private VaadinDropdownComponent supplierComboBox;
    @Getter
    private VaadinCheckboxComponent showPreviouslyOrderedElementsCheckBox;
    @Getter
    private VaadinDropdownComponent currencyComboBox;
    @Getter
    private VaadinDropdownComponent paymentTypeComboBox;
    @Getter
    private VaadinButtonComponent createOrderButton;

    public OrderFromSupplierPage(WebDriver driver, int port) {
        super(driver, port);

        initWebElements();
    }

    public PerformCreateResult performCreate(String supplierName) throws NoSuchElementException {
        for (int i = 0; i < supplierComboBox.getElementNumber(); i++) {
            if (supplierName == null) {
                supplierComboBox.fillWithRandom();
            }
            if (supplierName != null) {
                supplierComboBox.fillWith(supplierName);
            }
            grid.waitForRefresh();
            if (grid.getTotalRowNumber() == 0) {
                LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
                withData.put("Customer", supplierComboBox.getSelectedElement());
                getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
                OrderElementPage oePage = new OrderElementPage(getDriver(), getPort());
                oePage.performCreate(withData);
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
    public OrderFromSupplierPage initWebElements() {
        grid = new VaadinMultipleSelectGridComponent(getDriver(), By.xpath(multipleSelectGridXpath));
        supplierComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(supplierComboBoxXpath));
        showPreviouslyOrderedElementsCheckBox = new VaadinCheckboxComponent(getDriver(), By.xpath(showPreviouslyOrderedElementsCheckboxXpath));
        currencyComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(currencyComboBoxXpath));
        paymentTypeComboBox = new VaadinDropdownComponent(getDriver(), By.xpath(paymentTypeComboBoxXpath));
        createOrderButton = new VaadinButtonComponent(getDriver(), By.xpath(createOrderButtonXpath));
        sideMenu = new SideMenu(getDriver(), By.xpath(sideMenuXpath));
        return this;
    }
}

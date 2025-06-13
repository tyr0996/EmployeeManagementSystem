package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.*;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderCreateToCustomerTest extends BaseCrudTest {
    @Test
    public void createOrderTest() throws SQLException {
        createOrder();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void cannotGetOrderObjectFromOrderIdOnBeforeEnter() throws SQLException {
        Long number = dp.countElementsInTable("orders", "customer_customer_id is not null and supplier_supplier_id is null");
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);

        if (number == 0) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
            LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
            withData.put("Customer", "Erdei Róbert");
            withData.put("Supplier", "");

            OrderElementPage oePage = new OrderElementPage(driver, port);
            oePage.performCreate(withData);
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);
            OrderFromSupplierPage osPage = new OrderFromSupplierPage(driver, port);
            osPage.getSupplierComboBox().fillWith("Szállító1");
            osPage.getGrid().waitForRefresh();
            osPage.getGrid().selectElements(1);
            osPage.getCurrencyComboBox().fillWithRandom();
            osPage.getPaymentTypeComboBox().fillWithRandom();
            osPage.getCreateOrderButton().click();
            VaadinNotificationComponent n = new VaadinNotificationComponent(driver);
            n.close();
        }

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage oPage = new OrderPage(driver, port);
        oPage.getGrid().applyFilter("", "(C) ", "", "", "");
        oPage.getGrid().waitForRefresh();
        SoftAssert sa = new SoftAssert();
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 7);
        oPage.getGrid().getModifyButton(0).click();
        OrderFromSupplierPage page = new OrderFromSupplierPage(driver, port);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "Database error");
        notification.close();

        sa.assertEquals(page.getGrid().getTotalRowNumber(), 0);

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void customerNotSelectedShowPreviouslyGridIsEmptyTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        assertEquals(orderCreateToCustomerPage.getGrid().countVisibleDataRows(), 0);
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(true);
        assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(false);
        assertEquals(orderCreateToCustomerPage.getGrid().countVisibleDataRows(), 0);
        assertNull(VaadinNotificationComponent.hasNotification(driver));

    }

    public void createOrder() throws SQLException {
        createOrder(null, true, null, null);
    }

    public void createOrder(String notificationText, Boolean requiredSuccess, DataSource spyDataSource, Integer preSuccess) throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage oPage = new OrderPage(driver, port);

        int originalOrderNumber = oPage.getGrid().getTotalRowNumber();
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage page = new OrderCreateToCustomerPage(driver, port);
        page.getCustomerComboBox().fillWithRandom();
        page.getGrid().waitForRefresh();
        String customerName = page.getCustomerComboBox().getSelectedElement();
        int originalOrderElements = page.getGrid().getTotalRowNumber();
        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
        OrderElementPage oePage = new OrderElementPage(driver, port);

        LinkedHashMap<String, Object> sameUser = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "", "null", "", "", "", "", customerName};
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", null);
        oePage.performCreate(sameUser);
        oePage.performCreate(sameUser);
        oePage.performCreate(sameUser);
        oePage.performCreate(sameUser);
        oePage.performCreate(sameUser);
        oePage.getGrid().applyFilter(orderElementGridCustomerFilter);
        oePage.performDelete();
        oePage.performDelete();
        oePage.getGrid().resetFilter();
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU); //TODO megoldani, hogy ezt az oePage-ből tudjam meghívni a getSideMenu()-t
        OrderCreateToCustomerPage ocPage = new OrderCreateToCustomerPage(driver, port);
        ocPage.getCustomerComboBox().fillWith(customerName);
        ocPage.getGrid().waitForRefresh();

        assertEquals(ocPage.getGrid().getTotalRowNumber(), originalOrderElements + 3);

        ocPage.getGrid().selectElements(2);
        ocPage.getCurrencyComboBox().fillWithRandom();
        ocPage.getPaymentTypeComboBox().fillWithRandom();
        if (spyDataSource != null) {
            MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, preSuccess);
        }
        ocPage.getCreateOrderButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        if (notificationText == null) {
            assertThat(notification.getText()).contains("Order saved: ");
        } else {
            assertEquals(notification.getText(), notificationText);
        }
        notification.close();
        ocPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        oPage.initWebElements();
        assertEquals(oePage.getGrid().getTotalRowNumber(), requiredSuccess ? originalOrderNumber + 1 : originalOrderNumber);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void nullResponseFromServiceWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        updateOrder("Order modifying failed: Database error", false, spyDataSource, 1);

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        createOrder("Order saving failed: Database error", false, spyDataSource, 1);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void gettingCustomersFailedTest() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 2);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        assertFalse(orderCreateToCustomerPage.getCustomerComboBox().isEnabled());
        assertEquals(orderCreateToCustomerPage.getCustomerComboBox().getErrorMessage(), "EmsError happened while getting customers");

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void getOrderElementsByCustomerFailedTest() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 5);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        orderCreateToCustomerPage.getCustomerComboBox().fillWithRandom();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened while getting order elements to the customer");
        notification.close();
        assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void getPendingCodeStoreFailedTest() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 93);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        createOrder("EmsError happened while getting \"Pending\" status", false, spyDataSource, 0);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void getPaymentTypesFailedTest() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 3);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        assertFalse(orderCreateToCustomerPage.getPaymentTypeComboBox().isEnabled());
        assertEquals(orderCreateToCustomerPage.getPaymentTypeComboBox().getErrorMessage(), "EmsError happened while getting payment methods");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void getCurrencyTypesFailedTest() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 4);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        assertFalse(orderCreateToCustomerPage.getCurrencyComboBox().isEnabled());
        assertEquals(orderCreateToCustomerPage.getCurrencyComboBox().getErrorMessage(), "EmsError happened while getting currencies");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void updateOrder() throws SQLException {
        updateOrder(null, true, null, null);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    public void updateOrder(String notificationText, Boolean requiredSuccess, DataSource spyDataSource, Integer preSuccess) throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage oPage = new OrderPage(driver, port);
        int originalOrderNumber = oPage.getGrid().getTotalRowNumber();
        if (originalOrderNumber == 0) {
            createOrder();
        }

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage orderPage = new OrderPage(driver, port);
        String[] customerOrderFilter = new String[]{"", "(C) ", "", "", ""};

        orderPage.getGrid().applyFilter(customerOrderFilter);
        ElementLocation randomLocation = orderPage.getGrid().getRandomLocation();
        orderPage.getGrid().goToPage(randomLocation.getPageNumber());
        String[] originalData = orderPage.getGrid().getDataFromRowLocation(randomLocation, true);

        orderPage.getGrid().resetFilter();
        orderPage.getGrid().applyFilter(originalData);
        assertEquals(orderPage.getGrid().getTotalRowNumber(), 1);
        orderPage.getGrid().resetFilter();
        orderPage.getGrid().getModifyButton(randomLocation.getRowIndex()).click();
        OrderCreateToCustomerPage page = new OrderCreateToCustomerPage(driver, port);

        page.getGrid().selectElements(1);
        page.getCurrencyComboBox().fillWithRandom();
        page.getPaymentTypeComboBox().fillWithRandom();
        if (spyDataSource != null) {
            MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, preSuccess);
        }

        page.getCreateOrderButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        if (notificationText == null) {
            assertThat(notification.getText()).contains("Order updated: ");
        } else {
            assertEquals(notification.getText(), notificationText);
        }
        notification.close();
        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        oPage.initWebElements();
        if (requiredSuccess) {
            assertEquals(oPage.getGrid().getTotalRowNumber(), originalOrderNumber);
            oPage.getGrid().applyFilter(originalData);
            assertEquals(oPage.getGrid().getTotalRowNumber(), 0);
            oPage.getGrid().resetFilter();
        } else {
            assertEquals(oPage.getGrid().getTotalRowNumber(), originalOrderNumber);
            oPage.getGrid().applyFilter(originalData);
            assertEquals(oPage.getGrid().getTotalRowNumber(), 1);
            oPage.getGrid().resetFilter();
        }
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void noneSelectedFromTheOrderCreationGrid() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage orderPage = new OrderPage(driver, port);
        int originalOrderNumber = orderPage.getGrid().getTotalRowNumber();

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
        OrderCreateToCustomerPage page = new OrderCreateToCustomerPage(driver, port);

        page.getCustomerComboBox().fillWithRandom();
        String customerName = page.getCustomerComboBox().getSelectedElement();
        int originalOrderElements = page.getGrid().getTotalRowNumber();

        page.getSideMenu().navigate(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        OrderElementPage oePage = new OrderElementPage(driver, port);

        LinkedHashMap<String, Object> sameCustomer = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "", "null", "", "", "", "", customerName};
        sameCustomer.put("Customer", customerName);
        sameCustomer.put("Supplier", null);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);

        oePage.getGrid().applyFilter(orderElementGridCustomerFilter);
        oePage.performDelete();
        oePage.performDelete();
        oePage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
        page.initWebElements();

        page.getCustomerComboBox().fillWith(customerName);
        page.getGrid().waitForRefresh();
        assertEquals(page.getGrid().getTotalRowNumber(), originalOrderElements + 3);

        page.getGrid().selectElements(0);
        page.getCurrencyComboBox().fillWithRandom();
        page.getPaymentTypeComboBox().fillWithRandom();
        page.getCreateOrderButton().click();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Order must contains at least one order element!");
        notification.close();
        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        orderPage.initWebElements();
        assertEquals(orderPage.getGrid().getTotalRowNumber(), originalOrderNumber);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseUnavailableWhenGettingAllByCustomer() throws SQLException, InterruptedException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage orderPage = new OrderPage(driver, port);
        Thread.sleep(100);

        int originalOrderNumber = orderPage.getGrid().getTotalRowNumber();

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
        OrderCreateToCustomerPage page = new OrderCreateToCustomerPage(driver, port);

        page.getCustomerComboBox().fillWithRandom();
        String customerName = page.getCustomerComboBox().getSelectedElement();
        int originalOrderElements = page.getGrid().getTotalRowNumber();
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
        OrderElementPage oePage = new OrderElementPage(driver, port);

        LinkedHashMap<String, Object> sameCustomer = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "", "null", "", "", "", "", customerName};
        sameCustomer.put("Customer", customerName);
        sameCustomer.put("Supplier", null);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);

        oePage.getGrid().applyFilter(orderElementGridCustomerFilter);
        oePage.performDelete();
        oePage.performDelete();
        oePage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
        page.initWebElements();

        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        page.getCustomerComboBox().fillWith(customerName);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened while getting order elements to the customer");
        notification.close();
        assertEquals(page.getGrid().getTotalRowNumber(), 0);

        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        orderPage.initWebElements();

        assertEquals(orderPage.getGrid().getTotalRowNumber(), originalOrderNumber);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void moreThanOneOrderExistsForCustomerEditOne() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage opage = new OrderPage(driver, port);
        int originalOrderNumber = opage.getGrid().getTotalRowNumber();

        for (int i = 0; i < 2; i++) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage page = new OrderCreateToCustomerPage(driver, port);

            page.getCustomerComboBox().fillWithRandom();
            String customerName = page.getCustomerComboBox().getSelectedElement();
            page.getGrid().waitForRefresh();
            int originalOrderElements = page.getGrid().getTotalRowNumber();

            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
            OrderElementPage oePage = new OrderElementPage(driver, port);
            LinkedHashMap<String, Object> sameCustomer = new LinkedHashMap<>();

            String[] orderElementGridCustomerFilter = new String[]{"", "", "", "null", "", "", "", "", customerName};
            sameCustomer.put("Customer", customerName);
            sameCustomer.put("Supplier", null);
            VaadinNotificationComponent.closeAll(driver);
            oePage.performCreate(sameCustomer);
            oePage.performCreate(sameCustomer);
            oePage.performCreate(sameCustomer);
            oePage.performCreate(sameCustomer);
            oePage.performCreate(sameCustomer);
            VaadinNotificationComponent.closeAll(driver);

            oePage.getGrid().applyFilter(orderElementGridCustomerFilter);
            oePage.performDelete();
            oePage.performDelete();
            oePage.getGrid().resetFilter();

            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage page_2 = new OrderCreateToCustomerPage(driver, port);
            page_2.initWebElements();
            page_2.getCustomerComboBox().fillWith(customerName);
            page_2.getGrid().waitForRefresh();
            assertEquals(page_2.getGrid().getTotalRowNumber(), originalOrderElements + 3);

            page_2.getGrid().selectElements(2);
            page_2.getPaymentTypeComboBox().fillWithRandom();
            page_2.getCurrencyComboBox().fillWithRandom();
            page_2.getCreateOrderButton().click();
            VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
            assertThat(notification.getText()).contains("Order saved: ");
            notification.close();
        }
        updateOrder();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    @Test
    public void noCustomerSelectedButShowPreviouslyEnabledThanGridWillBeIsEmpty() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(true);
        assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void deselectShowPreviouslyChangesGridSelectionMode() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        SoftAssert sa = new SoftAssert();
        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        sa.assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);

        int originalOrderElements = orderCreateToCustomerPage.getGrid().getTotalRowNumber();
        orderCreateToCustomerPage.getCustomerComboBox().fillWithRandom();
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(true);
        orderCreateToCustomerPage.getGrid().waitForRefresh();
        sa.assertFalse(orderCreateToCustomerPage.getGrid().isMultiSelectEnabled());
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(false);
        orderCreateToCustomerPage.getGrid().waitForRefresh();
        sa.assertTrue(orderCreateToCustomerPage.getGrid().isMultiSelectEnabled());

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }
}

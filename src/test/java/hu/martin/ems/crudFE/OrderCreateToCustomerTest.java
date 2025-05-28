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

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderCreateToCustomerTest extends BaseCrudTest {
//    public static final String customerComboBoxXpath = contentXpath + "/vaadin-form-layout[1]/vaadin-combo-box";
//    public static final String currencyComboBoxXpath = contentXpath + "/vaadin-form-layout[2]/vaadin-combo-box[1]";
//    public static final String paymentMethodComboBoxXpath = contentXpath + "/vaadin-form-layout[2]/vaadin-combo-box[2]";
//
//    private static final String orderElementShowDeletedXpath = OrderElementCrudTest.showDeletedCheckBoxXpath;
//    private static final String orderElementGridXpath = OrderElementCrudTest.gridXpath;
//    private static final String orderElementCreateButtonXpath = OrderElementCrudTest.createButtonXpath;
//
//    private static final String orderXpath = contentXpath + "/vaadin-grid";
//    public static final String orderCreateOrderButtonXpath = contentXpath + "/vaadin-form-layout[2]/vaadin-button";
//
//    public static final String previouslyOrderedCheckboxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
//
//    public static final SoftAssert softAssert = new SoftAssert();
//
//    private static CrudTestingUtil crudTestingUtil;
//    private static CrudTestingUtil orderElementCrudTestingUtil;
//
//    public static final String createOrderGridXpath = contentXpath + "/vaadin-grid";
//
//
//    private static final String mainMenu = UIXpaths.ORDERS_MENU;
//    private static final String subMenu = UIXpaths.ORDER_CREATE_TO_CUSTOMER_SUBMENU;
//
//
//    private GridTestingUtil gridTestingUtil;
//
//
//
//    @BeforeClass
//    public void setup() {
//        gridTestingUtil = new GridTestingUtil(driver);
//        init();
//    }

//    private void init(){
//        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "Order", null, createOrderGridXpath, null);
//        orderElementCrudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "OrderElement", orderElementShowDeletedXpath, orderElementGridXpath, orderElementCreateButtonXpath);
//    }

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

        if(number == 0){
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

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
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
//        page.initWebElements();
//
//        page.getCustomerComboBox().fillWith(customerName);
//        page.getGrid().waitForRefresh();
//        page.getGrid().init();

        assertEquals(ocPage.getGrid().getTotalRowNumber(), originalOrderElements + 3);

        ocPage.getGrid().selectElements(2);
        ocPage.getCurrencyComboBox().fillWithRandom();
        ocPage.getPaymentTypeComboBox().fillWithRandom();
        if(spyDataSource != null) {
            MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, preSuccess);
        }
        ocPage.getCreateOrderButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        if(notificationText == null){
            assertThat(notification.getText()).contains("Order saved: ");
        }
        else{
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
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 21);
//        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 21);
//         Mockito.doReturn(null).when(spyOrderService).update(any(Order.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        updateOrder("Order modifying failed: Database error", false, spyDataSource, 1);
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);

        assertNull(VaadinNotificationComponent.hasNotification(driver));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
//        Mockito.doReturn(null).when(spyOrderService).save(any(Order.class));
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 95);
//        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 95);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        createOrder("Order saving failed: Database error", false, spyDataSource, 1);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
//        assertNull(VaadinNotificationComponent.hasNotification(driver));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void gettingCustomersFailedTest() throws SQLException {
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 2);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 2);
//        Mockito.doReturn(null).when(spyCustomerService).findAll(false); //Controllerben opcionális paraméterként jön.
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        assertFalse(orderCreateToCustomerPage.getCustomerComboBox().isEnabled());
        assertEquals(orderCreateToCustomerPage.getCustomerComboBox().getErrorMessage(), "EmsError happened while getting customers");
//        checkField(customerComboBoxXpath, "EmsError happened while getting customers");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void getOrderElementsByCustomerFailedTest() throws SQLException {
//        Mockito.doReturn(null).when(spyOrderElementService).getByCustomer(any(Long.class));
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
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
//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath));
//        Thread.sleep(100);
//        gridTestingUtil.checkNotificationText("EmsError happened while getting order elements to the customer");
//        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    public void getPendingCodeStoreFailedTest() throws SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).findByName("Pending"); //ApiClint-ben getAllByName("Pending");
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 93);
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
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.PAYMENT_TYPES_CODESTORE_ID); //id:7
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 3);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        assertFalse(orderCreateToCustomerPage.getPaymentTypeComboBox().isEnabled());
        assertEquals(orderCreateToCustomerPage.getPaymentTypeComboBox().getErrorMessage(), "EmsError happened while getting payment methods");
//        checkField(paymentMethodComboBoxXpath, "EmsError happened while getting payment methods");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void getCurrencyTypesFailedTest() throws SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.CURRENCIES_CODESTORE_ID); //id 1
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 4);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        assertFalse(orderCreateToCustomerPage.getCurrencyComboBox().isEnabled());
        assertEquals(orderCreateToCustomerPage.getCurrencyComboBox().getErrorMessage(), "EmsError happened while getting currencies");
//        checkField(currencyComboBoxXpath, "EmsError happened while getting currencies");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void updateOrder() throws SQLException {
        updateOrder(null, true, null, null);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    public void updateOrder(String notificationText, Boolean requiredSuccess, DataSource spyDataSource, Integer preSuccess) throws SQLException {
//        init();
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage oPage = new OrderPage(driver, port);
//        Thread.sleep(100);
        int originalOrderNumber = oPage.getGrid().getTotalRowNumber();
//        int originalOrderNumber = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);
        if(originalOrderNumber == 0){
            createOrder();
        }

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        OrderPage orderPage = new OrderPage(driver, port);
        String[] customerOrderFilter = new String[]{"", "(C) ", "", "", ""};
//        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
//        Thread.sleep(100);
        orderPage.getGrid().applyFilter(customerOrderFilter);
        ElementLocation randomLocation = orderPage.getGrid().getRandomLocation();
//        ElementLocation randomLocation = gridTestingUtil.getRandomLocationFromGrid(createOrderGridXpath);
        orderPage.getGrid().goToPage(randomLocation.getPageNumber());
//        gridTestingUtil.goToPageInPaginatedGrid(createOrderGridXpath, randomLocation.getPageNumber());
        String[] originalData = orderPage.getGrid().getDataFromRowLocation(randomLocation, true);
//        String[] originalData = gridTestingUtil.getDataFromRowLocation(createOrderGridXpath, randomLocation);
//        Thread.sleep(200);
        orderPage.getGrid().resetFilter();
        orderPage.getGrid().applyFilter(originalData);
//        gridTestingUtil.applyFilter(createOrderGridXpath, originalData);
        assertEquals(orderPage.getGrid().getTotalRowNumber(), 1);
//        assertEquals(1, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
        orderPage.getGrid().resetFilter();
//        gridTestingUtil.resetFilter(createOrderGridXpath);
        orderPage.getGrid().getModifyButton(randomLocation.getRowIndex()).click();
//        gridTestingUtil.getModifyButton(createOrderGridXpath, randomLocation.getRowIndex()).click();
        OrderCreateToCustomerPage page = new OrderCreateToCustomerPage(driver, port);
//        Thread.sleep(200);
//        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath, 5000);

//        Thread.sleep(200);
        page.getGrid().selectElements(1);
        page.getCurrencyComboBox().fillWithRandom();
        page.getPaymentTypeComboBox().fillWithRandom();
        if(spyDataSource != null){
            MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, preSuccess);
        }
//        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 1);
//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        page.getCreateOrderButton().click();
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        if(notificationText == null){
            assertThat(notification.getText()).contains("Order updated: ");
//            gridTestingUtil.checkNotificationContainsTexts("Order updated:");
        }
        else{
            assertEquals(notification.getText(), notificationText);
//            gridTestingUtil.checkNotificationText(notificationText);
        }
        notification.close();
        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        oPage.initWebElements();
//        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
//        Thread.sleep(100);
        if(requiredSuccess){
            assertEquals(oPage.getGrid().getTotalRowNumber(), originalOrderNumber);
            oPage.getGrid().applyFilter(originalData);
            assertEquals(oPage.getGrid().getTotalRowNumber(), 0);
            oPage.getGrid().resetFilter();
        }
        else{
            assertEquals(oPage.getGrid().getTotalRowNumber(), originalOrderNumber);
            oPage.getGrid().applyFilter(originalData);
            assertEquals(oPage.getGrid().getTotalRowNumber(), 1);
            oPage.getGrid().resetFilter();
        }
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

//    @Test
//    public void getOrderElementsByOrderIdFailedWhenSaveOrder() throws SQLException {
////        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 90);
////        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 90);
////        Mockito.doReturn(null).when(spyOrderService).save(any(Order.class));
////        Mockito.doReturn(new EmsResponse(522, "")).when(spyOrderApiClient).save(any(Order.class));
////        Mockito.doReturn(new EmsResponse(522, "")).when(spyOrderApiClient).getOrderElements(any(Long.class));
//        createOrder("Order saving failed: Database error", false, spyDataSource, 1);
//    }

    @Test
    public void noneSelectedFromTheOrderCreationGrid() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage orderPage = new OrderPage(driver, port);
//        Thread.sleep(100);
        int originalOrderNumber = orderPage.getGrid().getTotalRowNumber();
//        int originalOrderNumber = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
        OrderCreateToCustomerPage page = new OrderCreateToCustomerPage(driver, port);
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        Thread.sleep(100);
        page.getCustomerComboBox().fillWithRandom();
        String customerName = page.getCustomerComboBox().getSelectedElement();
        int originalOrderElements = page.getGrid().getTotalRowNumber();
//        int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        page.getSideMenu().navigate(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        OrderElementPage oePage = new OrderElementPage(driver, port);
//        Thread.sleep(100);

//        gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, Object> sameCustomer = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
        sameCustomer.put("Customer", customerName);
        sameCustomer.put("Supplier", null);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);
        oePage.performCreate(sameCustomer);

        oePage.getGrid().applyFilter(orderElementGridCustomerFilter);
        oePage.performDelete(); //TODO: ez itt meghalt, mert a notification null volt.
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

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
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
//        page.getCreateOrderButton().click();

        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        orderPage.initWebElements();

        assertEquals(orderPage.getGrid().getTotalRowNumber(), originalOrderNumber);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

//    @Test
//    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
//        EmptyLoggedInVaadinPage loggedInPage =
//                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
//        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
//
//        OrderPage orderPage = new OrderPage(driver, port);
////        Thread.sleep(100);
//
//        int originalOrderNumber = orderPage.getGrid().getTotalRowNumber();
//        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
//        OrderCreateToCustomerPage page = new OrderCreateToCustomerPage(driver, port);
//
////        gridTestingUtil.navigateMenu(mainMenu, subMenu);
////        Thread.sleep(100);
////
////        WebElement customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
//        page.getCustomerComboBox().fillWithRandom();
//        String customerName = page.getCustomerComboBox().getSelectedElement();
//        int originalOrderElements = page.getGrid().getTotalRowNumber();
////        String customerName = gridTestingUtil.selectRandomFromComboBox(customerComboBox);
////        int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);
//
//        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
//        OrderElementPage oePage = new OrderElementPage(driver, port);
//        //gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
//        //Thread.sleep(100);
//
//        //gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
//        LinkedHashMap<String, Object> sameCustomer = new LinkedHashMap<>();
//
//        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
//        sameCustomer.put("Customer", customerName);
//        sameCustomer.put("Supplier", null);
//        oePage.performCreate(sameCustomer);
//        oePage.performCreate(sameCustomer);
//        oePage.performCreate(sameCustomer);
//        oePage.performCreate(sameCustomer);
//        oePage.performCreate(sameCustomer);
//        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
//
//        oePage.getGrid().applyFilter(orderElementGridCustomerFilter);
//        oePage.performDelete();
//        oePage.performDelete();
//        oePage.getGrid().resetFilter();
//        //gridTestingUtil.applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
//        //orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
//        //orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
//        //gridTestingUtil.resetFilter(orderElementGridXpath);
//        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
//        //gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        //Thread.sleep(100);
//        page.initWebElements();
//
//        page.getCustomerComboBox().fillWith(customerName);
//        assertEquals(page.getGrid().getTotalRowNumber(), originalOrderElements + 3);
//
//        //customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
//        //gridTestingUtil.selectElementByTextFromComboBox(customerComboBox, customerName);
//        //gridTestingUtil.selectRandomFromComboBox(customerComboBox);
//        //Thread.sleep(200);
//        //gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
//        //assertEquals(originalOrderElements + 3, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
//        page.getGrid().selectElements(2);
//        page.getCurrencyComboBox().fillWithRandom();
//        page.getPaymentTypeComboBox().fillWithRandom();
//
//        //gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
//        //gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
//        //gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));
//
//
////        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(this, spyDataSource, 1);
//        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 1);
//        page.getCreateOrderButton().click();
//        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
//        assertEquals(notification.getText(), "Order saving failed: Database error");
//        //gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
//        //gridTestingUtil.checkNotificationText("Order saving failed: Database error");
//
//        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDERS_MENU);
//        orderPage.initWebElements();
//        assertEquals(orderPage.getGrid().getTotalRowNumber(), originalOrderNumber);
//        //gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
//        //Thread.sleep(100);
//        //assertEquals(originalOrderNumber, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
//    }

    @Test
    public void moreThanOneOrderExistsForCustomerEditOne() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage opage = new OrderPage(driver, port);
        int originalOrderNumber = opage.getGrid().getTotalRowNumber();

        for(int i = 0; i < 2; i++){
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
//            page.initWebElements();
            OrderCreateToCustomerPage page = new OrderCreateToCustomerPage(driver, port);
            //gridTestingUtil.navigateMenu(mainMenu, subMenu);
            //Thread.sleep(100);

            page.getCustomerComboBox().fillWithRandom();
            String customerName = page.getCustomerComboBox().getSelectedElement();
            page.getGrid().waitForRefresh();
            int originalOrderElements = page.getGrid().getTotalRowNumber();
            //WebElement customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
            //String customerName = gridTestingUtil.selectRandomFromComboBox(customerComboBox);
            //int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
            OrderElementPage oePage = new OrderElementPage(driver, port);
            //gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
            //Thread.sleep(100);
            //gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
            LinkedHashMap<String, Object> sameCustomer = new LinkedHashMap<>();

            String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
            sameCustomer.put("Customer", customerName);
            sameCustomer.put("Supplier", null);
            VaadinNotificationComponent.closeAll(driver);
            oePage.performCreate(sameCustomer);
            oePage.performCreate(sameCustomer);
            oePage.performCreate(sameCustomer);
            oePage.performCreate(sameCustomer);
            oePage.performCreate(sameCustomer);
            VaadinNotificationComponent.closeAll(driver);

            //orderElementCrudTestingUtil.createTest(sameUser, "", true);
            //orderElementCrudTestingUtil.createTest(sameUser, "", true);
            //orderElementCrudTestingUtil.createTest(sameUser, "", true);
            //orderElementCrudTestingUtil.createTest(sameUser, "", true);
            //orderElementCrudTestingUtil.createTest(sameUser, "", true);
            oePage.getGrid().applyFilter(orderElementGridCustomerFilter);
            oePage.performDelete();
            oePage.performDelete();
            oePage.getGrid().resetFilter();
            //gridTestingUtil.applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
            ///orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
            //orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
            //gridTestingUtil.resetFilter(orderElementGridXpath);

            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage page_2 = new OrderCreateToCustomerPage(driver, port);
            page_2.initWebElements();
            page_2.getCustomerComboBox().fillWith(customerName);
            page_2.getGrid().waitForRefresh();
            assertEquals(page_2.getGrid().getTotalRowNumber(), originalOrderElements + 3);

            //gridTestingUtil.navigateMenu(mainMenu, subMenu);
            //Thread.sleep(100);

           // customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
            //gridTestingUtil.selectElementByTextFromComboBox(customerComboBox, customerName);
            //gridTestingUtil.selectRandomFromComboBox(customerComboBox);
            //Thread.sleep(200);
            //gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
            //assertEquals(originalOrderElements + 3, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
            page_2.getGrid().selectElements(2);
            page_2.getPaymentTypeComboBox().fillWithRandom();
            page_2.getCurrencyComboBox().fillWithRandom();
            page_2.getCreateOrderButton().click();
            VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
            assertThat(notification.getText()).contains("Order saved: ");
            notification.close();
            //gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
            //gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
            //gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));

            //gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
            //gridTestingUtil.checkNotificationContainsTexts("Order saved:");
        }
//        assertEquals(gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath), originalOrderNumber + 2);
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
        //assertEquals(gridTestingUtil.countVisibleGridDataRows(orderElementGridXpath), 0);
        //gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
        //assertEquals(gridTestingUtil.countVisibleGridDataRows(orderElementGridXpath), 0);
    }

    @Test
    public void deselectShowPreviouslyChangesGridSelectionMode() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);

        SoftAssert sa = new SoftAssert();
        OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
        sa.assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
        //assertEquals(gridTestingUtil.countVisibleGridDataRows(orderElementGridXpath), 0);

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

        //WebElement customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
        //int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);
        //gridTestingUtil.selectRandomFromComboBox(customerComboBox);

        //gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
//        softAssert.assertEquals(gridTestingUtil.isInMultiSelectMode(createOrderGridXpath), true);
        //gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, false);
        //softAssert.assertEquals(gridTestingUtil.isInMultiSelectMode(createOrderGridXpath), false);
    }

    private void checkField(String fieldXpath, String errorMessage){
        //assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(fieldXpath)), false, "A megadott mező enabled, pedig disabled kell: " + gridTestingUtil.findVisibleElementWithXpath(fieldXpath).getText());
        //assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(orderCreateOrderButtonXpath)), false);
        //assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(fieldXpath)), errorMessage);
    }
}

package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.OrderElementPage;
import hu.martin.ems.pages.OrderFromSupplierPage;
import hu.martin.ems.pages.OrderPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import org.checkerframework.common.reflection.qual.Invoke;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderFromSupplierTest extends BaseCrudTest {
//    public static final String supplierComboBoxXpath = contentXpath + "/vaadin-form-layout[1]/vaadin-combo-box";
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
//    private static final String subMenu = UIXpaths.ORDER_FROM_SUPPLIER_SUBMENU;
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
    }

    @Test
    public void supplierNotSelectedShowPreviouslyGridIsEmptyTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        assertEquals(orderCreateToCustomerPage.getGrid().countVisibleDataRows(), 0);
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(true);
        assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(false);
        assertEquals(orderCreateToCustomerPage.getGrid().countVisibleDataRows(), 0);

//        int originalRows = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);
//        WebElement previously = gridTestingUtil.findVisibleElementWithXpath(previouslyOrderedCheckboxXpath);
//        gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
//        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
//        gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, false);
//        assertEquals(originalRows, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
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
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage page = new OrderFromSupplierPage(driver, port);
        page.getSupplierComboBox().fillWithRandom();
        String supplierName = page.getSupplierComboBox().getSelectedElement();
        int originalOrderElements = page.getGrid().getTotalRowNumber();
        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
        OrderElementPage oePage = new OrderElementPage(driver, port);

        LinkedHashMap<String, Object> sameUser = new LinkedHashMap<>();

        String[] orderElementGridSupplierFilter = new String[]{"", "", "null", "", "", "", "", supplierName};
        sameUser.put("Supplier", supplierName);
        sameUser.put("Customer", null);
        oePage.performCreate(sameUser);
        oePage.performCreate(sameUser);
        oePage.performCreate(sameUser);
        oePage.performCreate(sameUser);
        oePage.performCreate(sameUser);
        oePage.getGrid().applyFilter(orderElementGridSupplierFilter);
        oePage.performDelete();
        oePage.performDelete();
        oePage.getGrid().resetFilter();
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU); //TODO megoldani, hogy ezt az oePage-ből tudjam meghívni a getSideMenu()-t
        OrderFromSupplierPage ocPage = new OrderFromSupplierPage(driver, port);
        ocPage.getSupplierComboBox().fillWith(supplierName);
        ocPage.getGrid().waitForRefresh();
//        page.initWebElements();
//
//        page.getSupplierComboBox().fillWith(supplierName);
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
    }

    @Test
    public void nullResponseFromServiceWhenModify() throws SQLException {
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 21);
//        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 21);
//         Mockito.doReturn(null).when(spyOrderService).update(any(Order.class));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        updateOrder("Order modifying failed: Database error", false, spyDataSource, 1);
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);

        assertFalse(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
//        Mockito.doReturn(null).when(spyOrderService).save(any(Order.class));
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 95);
//        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 95);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        createOrder("Order saving failed: Database error", false, spyDataSource, 1);
        assertFalse(VaadinNotificationComponent.hasNotification(driver));
//        assertFalse(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void gettingSuppliersFailedTest() throws SQLException {
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 2);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 2);
//        Mockito.doReturn(null).when(spySupplierService).findAll(false); //Controllerben opcionális paraméterként jön.
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        assertFalse(orderCreateToCustomerPage.getSupplierComboBox().isEnabled());
        assertEquals(orderCreateToCustomerPage.getSupplierComboBox().getErrorMessage(), "EmsError happened while getting suppliers");
//        checkField(supplierComboBoxXpath, "EmsError happened while getting suppliers");
        assertFalse(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void getOrderElementsBySupplierFailedTest() throws SQLException {
//        Mockito.doReturn(null).when(spyOrderElementService).getBySupplier(any(Long.class));
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 5);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        orderCreateToCustomerPage.getSupplierComboBox().fillWithRandom();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened while getting order elements to the supplier");
        notification.close();
        assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(supplierComboBoxXpath));
//        Thread.sleep(100);
//        gridTestingUtil.checkNotificationText("EmsError happened while getting order elements to the supplier");
//        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    public void getPendingCodeStoreFailedTest() throws SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).findByName("Pending"); //ApiClint-ben getAllByName("Pending");
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 93);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 93);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        createOrder("EmsError happened while getting \"Pending\" status", false, spyDataSource, 0);
    }

    @Test
    public void getPaymentTypesFailedTest() throws SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.PAYMENT_TYPES_CODESTORE_ID); //id:7
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 3);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        assertFalse(orderCreateToCustomerPage.getPaymentTypeComboBox().isEnabled());
        assertEquals(orderCreateToCustomerPage.getPaymentTypeComboBox().getErrorMessage(), "EmsError happened while getting payment methods");
//        checkField(paymentMethodComboBoxXpath, "EmsError happened while getting payment methods");
        assertFalse(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void getCurrencyTypesFailedTest() throws SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.CURRENCIES_CODESTORE_ID); //id 1
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 4);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        assertFalse(orderCreateToCustomerPage.getCurrencyComboBox().isEnabled());
        assertEquals(orderCreateToCustomerPage.getCurrencyComboBox().getErrorMessage(), "EmsError happened while getting currencies");
//        checkField(currencyComboBoxXpath, "EmsError happened while getting currencies");
        assertFalse(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void updateOrder() throws SQLException {
        updateOrder(null, true, null, null);
    }

    public void updateOrder(String notificationText, Boolean requiredSuccess, DataSource spyDataSource, Integer preSuccess) throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage oPage = new OrderPage(driver, port);
        int originalOrderNumber = oPage.getGrid().getTotalRowNumber();
        String[] supplierOrderFilter = new String[]{"", "(S) ", "", "", ""};
        oPage.getGrid().applyFilter(supplierOrderFilter);

//        if(originalOrderNumber == 0){
//            createOrder();
//            originalOrderNumber = oPage.getGrid().getTotalRowNumber();
//        }

//        String[] supplierOrderFilter = new String[]{"", "(S) ", "", "", ""};
//        oPage.getGrid().applyFilter(supplierOrderFilter);
        ElementLocation randomLocation = oPage.getGrid().getRandomLocation();
        if(randomLocation == null){
            createOrder();
            randomLocation = new ElementLocation(1, 0);
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
            oPage.initWebElements();
            originalOrderNumber = oPage.getGrid().getTotalRowNumber();
            oPage.getGrid().applyFilter(supplierOrderFilter);
        }
        oPage.getGrid().goToPage(randomLocation.getPageNumber());
        String[] originalData = oPage.getGrid().getDataFromRowLocation(randomLocation, true);
        oPage.getGrid().resetFilter();
        oPage.getGrid().applyFilter(originalData);
        assertEquals(oPage.getGrid().getTotalRowNumber(), 1);
        oPage.getGrid().resetFilter();
        oPage.getGrid().applyFilter(supplierOrderFilter);
        oPage.getGrid().getModifyButton(randomLocation.getRowIndex()).click();
        OrderFromSupplierPage page = new OrderFromSupplierPage(driver, port);

        page.getGrid().selectElements(1);
        page.getCurrencyComboBox().fillWithRandom();
        page.getPaymentTypeComboBox().fillWithRandom();
        if(spyDataSource != null){
            MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, preSuccess);
        }

        page.getCreateOrderButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        if(notificationText == null){
            assertThat(notification.getText()).contains("Order updated: ");
        }
        else{
            assertEquals(notification.getText(), notificationText);
        }
        notification.close();
        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        oPage.initWebElements();
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

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);
        OrderFromSupplierPage page = new OrderFromSupplierPage(driver, port);
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        Thread.sleep(100);
        page.getSupplierComboBox().fillWithRandom();
        String supplierName = page.getSupplierComboBox().getSelectedElement();
        int originalOrderElements = page.getGrid().getTotalRowNumber();
//        int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        page.getSideMenu().navigate(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        OrderElementPage oePage = new OrderElementPage(driver, port);
//        Thread.sleep(100);

//        gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, Object> sameSupplier = new LinkedHashMap<>();

        String[] orderElementGridSupplierFilter = new String[]{"", "", "null", "", "", "", "", supplierName};
        sameSupplier.put("Supplier", supplierName);
        sameSupplier.put("Customer", null);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);

        oePage.getGrid().applyFilter(orderElementGridSupplierFilter);
        oePage.performDelete(); //TODO: ez itt meghalt, mert a notification null volt.
        oePage.performDelete();
        oePage.getGrid().resetFilter();
//        orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        orderElementCrudTestingUtil.createTest(sameUser, "", true);


//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        Thread.sleep(100);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);
        page.initWebElements();
//        supplierComboBox = gridTestingUtil.findVisibleElementWithXpath(supplierComboBoxXpath);

        page.getSupplierComboBox().fillWith(supplierName);
        page.getGrid().waitForRefresh();

//        gridTestingUtil.selectElementByTextFromComboBox(supplierComboBox, supplierName);
        //gridTestingUtil.selectRandomFromComboBox(supplierComboBox);
//        Thread.sleep(200);
//        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(page.getGrid().getTotalRowNumber(), originalOrderElements + 3);

        page.getGrid().selectElements(0);
        page.getCurrencyComboBox().fillWithRandom();
        page.getPaymentTypeComboBox().fillWithRandom();
        page.getCreateOrderButton().click();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Order must contains at least one order element!");
        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        orderPage.initWebElements();
        assertEquals(orderPage.getGrid().getTotalRowNumber(), originalOrderNumber);


//        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 0);
//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));
//
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
//        gridTestingUtil.checkNotificationText("Order must contains at least one order element!");

//        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
//        Thread.sleep(100);
//        assertEquals(originalOrderNumber, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test

    public void databaseUnavailableWhenGettingAllBySupplier() throws SQLException, InterruptedException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage orderPage = new OrderPage(driver, port);
        Thread.sleep(100);

        int originalOrderNumber = orderPage.getGrid().getTotalRowNumber();

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);
        OrderFromSupplierPage page = new OrderFromSupplierPage(driver, port);

//        WebElement supplierComboBox = gridTestingUtil.findVisibleElementWithXpath(supplierComboBoxXpath);
        page.getSupplierComboBox().fillWithRandom();
        String supplierName = page.getSupplierComboBox().getSelectedElement();
        int originalOrderElements = page.getGrid().getTotalRowNumber();
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
        OrderElementPage oePage = new OrderElementPage(driver, port);
//        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
//        Thread.sleep(100);

//        gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, Object> sameSupplier = new LinkedHashMap<>();

        String[] orderElementGridSupplierFilter = new String[]{"", "", "null", "", "", "", "", supplierName};
        sameSupplier.put("Supplier", supplierName);
        sameSupplier.put("Supplier", null);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);

        oePage.getGrid().applyFilter(orderElementGridSupplierFilter);
        oePage.performDelete(); //TODO: ez itt meghalt, mert a notification null volt.
        oePage.performDelete();
        oePage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);
        page.initWebElements();
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        Thread.sleep(100);

//        supplierComboBox = gridTestingUtil.findVisibleElementWithXpath(supplierComboBoxXpath);

//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(this, spyDataSource, 0);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        page.getSupplierComboBox().fillWith(supplierName);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened while getting order elements to the supplier");
//        gridTestingUtil.selectElementByTextFromComboBox(supplierComboBox, supplierName);
        //gridTestingUtil.selectRandomFromComboBox(supplierComboBox);
//        Thread.sleep(200);
//        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(page.getGrid().getTotalRowNumber(), 0);

//        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
//        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));
        page.getCreateOrderButton().click();

        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);
        orderPage.initWebElements();
        //gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        //gridTestingUtil.checkNotificationText("EmsError happened while getting order elements to the supplier");

//        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
//        Thread.sleep(100);
        assertEquals(orderPage.getGrid().getTotalRowNumber(), originalOrderNumber);
//        assertEquals(originalOrderNumber, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
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
//        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);
//        OrderFromSupplierPage page = new OrderFromSupplierPage(driver, port);
//
////        gridTestingUtil.navigateMenu(mainMenu, subMenu);
////        Thread.sleep(100);
////
////        WebElement supplierComboBox = gridTestingUtil.findVisibleElementWithXpath(supplierComboBoxXpath);
//        page.getSupplierComboBox().fillWithRandom();
//        String supplierName = page.getSupplierComboBox().getSelectedElement();
//        int originalOrderElements = page.getGrid().getTotalRowNumber();
////        String supplierName = gridTestingUtil.selectRandomFromComboBox(supplierComboBox);
////        int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);
//
//        page.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
//        OrderElementPage oePage = new OrderElementPage(driver, port);
//        //gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
//        //Thread.sleep(100);
//
//        //gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
//        LinkedHashMap<String, Object> sameSupplier = new LinkedHashMap<>();
//
//        String[] orderElementGridSupplierFilter = new String[]{"", "", "null", "", "", "", "", supplierName};
//        sameSupplier.put("Supplier", supplierName);
//        sameSupplier.put("Supplier", null);
//        oePage.performCreate(sameSupplier);
//        oePage.performCreate(sameSupplier);
//        oePage.performCreate(sameSupplier);
//        oePage.performCreate(sameSupplier);
//        oePage.performCreate(sameSupplier);
//        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
//        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
//
//        oePage.getGrid().applyFilter(orderElementGridSupplierFilter);
//        oePage.performDelete();
//        oePage.performDelete();
//        oePage.getGrid().resetFilter();
//        //gridTestingUtil.applyFilter(orderElementGridXpath, orderElementGridSupplierFilter);
//        //orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridSupplierFilter);
//        //orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridSupplierFilter);
//        //gridTestingUtil.resetFilter(orderElementGridXpath);
//        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);
//        //gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        //Thread.sleep(100);
//        page.initWebElements();
//
//        page.getSupplierComboBox().fillWith(supplierName);
//        assertEquals(page.getGrid().getTotalRowNumber(), originalOrderElements + 3);
//
//        //supplierComboBox = gridTestingUtil.findVisibleElementWithXpath(supplierComboBoxXpath);
//        //gridTestingUtil.selectElementByTextFromComboBox(supplierComboBox, supplierName);
//        //gridTestingUtil.selectRandomFromComboBox(supplierComboBox);
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
    public void moreThanOneOrderExistsForSupplierEditOne() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_SUBMENU);

        OrderPage opage = new OrderPage(driver, port);
        int originalOrderNumber = opage.getGrid().getTotalRowNumber();

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);
//            page.initWebElements();
        OrderFromSupplierPage page = new OrderFromSupplierPage(driver, port);
        //gridTestingUtil.navigateMenu(mainMenu, subMenu);
        //Thread.sleep(100);

        page.getSupplierComboBox().fillWithRandom();
        String supplierName = page.getSupplierComboBox().getSelectedElement();
        page.getGrid().waitForRefresh();
        int originalOrderElements = page.getGrid().getTotalRowNumber();
        //WebElement supplierComboBox = gridTestingUtil.findVisibleElementWithXpath(supplierComboBoxXpath);
        //String supplierName = gridTestingUtil.selectRandomFromComboBox(supplierComboBox);
        //int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
        OrderElementPage oePage = new OrderElementPage(driver, port);
        //gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        //Thread.sleep(100);
        //gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, Object> sameSupplier = new LinkedHashMap<>();

        String[] orderElementGridSupplierFilter = new String[]{"", "", "null", "", "", "", "", supplierName};
        sameSupplier.put("Supplier", supplierName);
        sameSupplier.put("Customer", null);
        VaadinNotificationComponent.closeAll(driver);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);
        oePage.performCreate(sameSupplier);

        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
        //orderElementCrudTestingUtil.createTest(sameUser, "", true);
        oePage.getGrid().applyFilter(orderElementGridSupplierFilter);
        oePage.performDelete();
        oePage.performDelete();
        oePage.getGrid().resetFilter();
        //gridTestingUtil.applyFilter(orderElementGridXpath, orderElementGridSupplierFilter);
        ///orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridSupplierFilter);
        //orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridSupplierFilter);
        //gridTestingUtil.resetFilter(orderElementGridXpath);

        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);
        OrderFromSupplierPage page_2 = new OrderFromSupplierPage(driver, port);
        page_2.initWebElements();
        page_2.getSupplierComboBox().fillWith(supplierName);
        page_2.getGrid().waitForRefresh();
        assertEquals(page_2.getGrid().getTotalRowNumber(), originalOrderElements + 3);

        //gridTestingUtil.navigateMenu(mainMenu, subMenu);
        //Thread.sleep(100);

        // supplierComboBox = gridTestingUtil.findVisibleElementWithXpath(supplierComboBoxXpath);
        //gridTestingUtil.selectElementByTextFromComboBox(supplierComboBox, supplierName);
        //gridTestingUtil.selectRandomFromComboBox(supplierComboBox);
        //Thread.sleep(200);
        //gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
        //assertEquals(originalOrderElements + 3, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
        page_2.getGrid().selectElements(2);
        page_2.getPaymentTypeComboBox().fillWithRandom();
        page_2.getCurrencyComboBox().fillWithRandom();
        page_2.getCreateOrderButton().click();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertThat(notification.getText()).contains("Order saved: ");
        //gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
        //gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
        //gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        //gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        //gridTestingUtil.checkNotificationContainsTexts("Order saved:");
        updateOrder();

    }


    @Test
    public void noSupplierSelectedButShowPreviouslyEnabledThanGridWillBeIsEmpty() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(true);
        assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
        //assertEquals(gridTestingUtil.countVisibleGridDataRows(orderElementGridXpath), 0);
        //gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
        //assertEquals(gridTestingUtil.countVisibleGridDataRows(orderElementGridXpath), 0);
    }

    @Test
    public void deselectShowPreviouslyChangesGridSelectionMode() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_FROM_SUPPLIER_SUBMENU);

        SoftAssert sa = new SoftAssert();
        OrderFromSupplierPage orderCreateToCustomerPage = new OrderFromSupplierPage(driver, port);
        sa.assertEquals(orderCreateToCustomerPage.getGrid().getTotalRowNumber(), 0);
        //assertEquals(gridTestingUtil.countVisibleGridDataRows(orderElementGridXpath), 0);

        int originalOrderElements = orderCreateToCustomerPage.getGrid().getTotalRowNumber();
        orderCreateToCustomerPage.getSupplierComboBox().fillWithRandom();
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(true);
        orderCreateToCustomerPage.getGrid().waitForRefresh();
        sa.assertFalse(orderCreateToCustomerPage.getGrid().isMultiSelectEnabled());
        orderCreateToCustomerPage.getShowPreviouslyOrderedElementsCheckBox().setStatus(false);
        orderCreateToCustomerPage.getGrid().waitForRefresh();
        sa.assertTrue(orderCreateToCustomerPage.getGrid().isMultiSelectEnabled());

        sa.assertAll();

        //WebElement supplierComboBox = gridTestingUtil.findVisibleElementWithXpath(supplierComboBoxXpath);
        //int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);
        //gridTestingUtil.selectRandomFromComboBox(supplierComboBox);

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

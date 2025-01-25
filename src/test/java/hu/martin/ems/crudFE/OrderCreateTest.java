package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.model.Order;
import org.mockito.Mockito;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class OrderCreateTest extends BaseCrudTest {

    public static final String customerComboBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-form-layout[1]/vaadin-combo-box";
    public static final String currencyComboBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-form-layout[2]/vaadin-combo-box[1]";
    public static final String paymentMethodComboBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-form-layout[2]/vaadin-combo-box[2]";
    private static final String orderElementShowDeletedXpath = OrderElementCrudTest.showDeletedChecBoxXpath;
    private static final String orderElementGridXpath = OrderElementCrudTest.gridXpath;
    private static final String orderElementCreateButtonXpath = OrderElementCrudTest.createButtonXpath;
    private static final String orderXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid";
    public static final String orderCreateOrderButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-form-layout[2]/vaadin-button";

    public static final String previouslyOrderedCheckboxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";

    public static final SoftAssert softAssert = new SoftAssert();

    private static CrudTestingUtil crudTestingUtil;
    private static CrudTestingUtil orderElementCrudTestingUtil;

    public static final String createOrderGridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";


    private static final String mainMenu = UIXpaths.ORDERS_MENU;
    private static final String subMenu = UIXpaths.ORDER_CREATE_SUBMENU;


    @BeforeClass
    public static void setupTest(){
        init();
    }

    private static void init(){
        crudTestingUtil = new CrudTestingUtil(driver, "Order", null, createOrderGridXpath, null);
        orderElementCrudTestingUtil = new CrudTestingUtil(driver, "OrderElement", orderElementShowDeletedXpath, orderElementGridXpath, orderElementCreateButtonXpath);
        GridTestingUtil.driver = driver;
    }

    @Test
    public void createOrderTest() throws InterruptedException {
        createOrder();
    }

    @Test
    public void customerNotSelectedShowPreviouslyGridIsEmptyTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        int originalRows = countVisibleGridDataRows(createOrderGridXpath);

        WebElement previously = findVisibleElementWithXpath(previouslyOrderedCheckboxXpath);
        setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
        assertEquals(0, countVisibleGridDataRows(createOrderGridXpath));
        setCheckboxStatus(previouslyOrderedCheckboxXpath, false);
        assertEquals(originalRows, countVisibleGridDataRows(createOrderGridXpath));
    }

    public static void createOrder() throws InterruptedException {
        createOrder(null, true);
    }

    public static void createOrder(String notificationText, Boolean requiredSuccess) throws InterruptedException {
        init();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        WebElement customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        String customerName = selectRandomFromComboBox(customerComboBox);
        int originalOrderElements = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);

        applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        resetFilter(orderElementGridXpath);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        selectElementByTextFromComboBox(customerComboBox, customerName);
        //selectRandomFromComboBox(customerComboBox);
        Thread.sleep(200);
        findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(originalOrderElements + 3, countVisibleGridDataRows(createOrderGridXpath)); //TODO valamiért ez mindig 0-ra jön ki éles futáskor.

        selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
        selectRandomFromComboBox(findVisibleElementWithXpath(currencyComboBoxXpath));
        selectRandomFromComboBox(findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        if(notificationText == null){
            checkNotificationContainsTexts("Order saved:");
        }
        else{
            checkNotificationText(notificationText);
        }

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        if(requiredSuccess){
            assertEquals(originalOrderNumber + 1, countVisibleGridDataRows(createOrderGridXpath));
        }
        else{
            assertEquals(originalOrderNumber, countVisibleGridDataRows(createOrderGridXpath));
        }
    }

    @Test
    public void nullResponseFromServiceWhenModify() throws InterruptedException {
         Mockito.doReturn(null).when(spyOrderService).update(any(Order.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        updateOrder("Order saving failed: Internal server error", false);
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws InterruptedException {
        Mockito.doReturn(null).when(spyOrderService).save(any(Order.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        createOrder("Order saving failed: Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void gettingCustomersFailedTest() {
        Mockito.doReturn(null).when(spyCustomerService).findAll(false); //Controllerben opcionális paraméterként jön.
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkField(customerComboBoxXpath, "Error happened while getting customers");
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void getOrderElementsByCustomerFailedTest() throws InterruptedException {
        Mockito.doReturn(null).when(spyOrderElementService).getByCustomer(any(Long.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        selectRandomFromComboBox(findVisibleElementWithXpath(customerComboBoxXpath));
        Thread.sleep(100);
        checkNotificationText("Error happened while getting order elements to the customer");
        assertEquals(0, countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    public void getPendingCodeStoreFailedTest() throws InterruptedException {
        Mockito.doReturn(null).when(spyCodeStoreService).findByName("Pending"); //ApiClint-ben getAllByName("Pending");
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        createOrder("Error happened while getting \"Pending\" status", false);
    }

    @Test
    public void getPaymentTypesFailedTest(){
        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.PAYMENT_TYPES_CODESTORE_ID);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkField(paymentMethodComboBoxXpath, "Error happened while getting payment methods");
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void getCurrencyTypesFailedTest(){
        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.CURRENCIES_CODESTORE_ID);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkField(currencyComboBoxXpath, "Error happened while getting currencies");
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void updateOrder() throws InterruptedException {
        updateOrder(null, true);
    }

    public static void updateOrder(String notificationText, Boolean requiredSuccess) throws InterruptedException {
        init();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        int originalOrderNumber = countVisibleGridDataRows(createOrderGridXpath);
        if(originalOrderNumber == 0){
            createOrder();
        }

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        ElementLocation randomLocation = getRandomLocationFromGrid(createOrderGridXpath);
        goToPageInPaginatedGrid(createOrderGridXpath, randomLocation.getPageNumber());
        String[] originalData = getDataFromRowLocation(createOrderGridXpath, randomLocation);
        applyFilter(createOrderGridXpath, originalData);
        assertEquals(1, countVisibleGridDataRows(createOrderGridXpath));
        resetFilter(createOrderGridXpath);

        getModifyButton(createOrderGridXpath, randomLocation.getRowIndex()).click();

        Thread.sleep(200);
        findVisibleElementWithXpath(createOrderGridXpath);

        selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 1);
        selectRandomFromComboBox(findVisibleElementWithXpath(currencyComboBoxXpath));
        selectRandomFromComboBox(findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        if(notificationText == null){
            checkNotificationContainsTexts("Order updated:");
        }
        else{
            checkNotificationText(notificationText);
        }

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        if(requiredSuccess){
            assertEquals(originalOrderNumber, countVisibleGridDataRows(createOrderGridXpath));
            applyFilter(createOrderGridXpath, originalData);
            assertEquals(0, countVisibleGridDataRows(createOrderGridXpath));
            resetFilter(createOrderGridXpath);
        }
        else{
            assertEquals(originalOrderNumber, countVisibleGridDataRows(createOrderGridXpath));
            applyFilter(createOrderGridXpath, originalData);
            assertEquals(1, countVisibleGridDataRows(createOrderGridXpath));
            resetFilter(createOrderGridXpath);
        }
    }

    @Test
    public void getOrderElementsByOrderIdFailedWhenSaveOrder() throws InterruptedException {
        Mockito.doReturn(null).when(spyOrderService).save(any(Order.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyOrderApiClient).save(any(Order.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyOrderApiClient).getOrderElements(any(Long.class));
        createOrder("Order saving failed: Internal server error", false);
    }

    @Test
    public void noneSelectedFromTheOrderCreationGrid() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        init();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        WebElement customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        String customerName = selectRandomFromComboBox(customerComboBox);
        int originalOrderElements = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);

        applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        resetFilter(orderElementGridXpath);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);

        selectElementByTextFromComboBox(customerComboBox, customerName);
        //selectRandomFromComboBox(customerComboBox);
        Thread.sleep(200);
        findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(countVisibleGridDataRows(createOrderGridXpath), originalOrderElements + 3);

        selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 0);
        selectRandomFromComboBox(findVisibleElementWithXpath(currencyComboBoxXpath));
        selectRandomFromComboBox(findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        checkNotificationText("Order must contains at least one order element!");

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        assertEquals(originalOrderNumber, countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    public void databaseUnavailableWhenGettingAllByCustomer() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        init();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        WebElement customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        String customerName = selectRandomFromComboBox(customerComboBox);
        int originalOrderElements = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);

        applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        resetFilter(orderElementGridXpath);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);

        mockDatabaseNotAvailable(spyDataSource, 0);

        selectElementByTextFromComboBox(customerComboBox, customerName);
        //selectRandomFromComboBox(customerComboBox);
        Thread.sleep(200);
        findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(countVisibleGridDataRows(createOrderGridXpath), 0);

        selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
        selectRandomFromComboBox(findVisibleElementWithXpath(currencyComboBoxXpath));
        selectRandomFromComboBox(findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        checkNotificationText("Error happened while getting order elements to the customer");

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        assertEquals(originalOrderNumber, countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        init();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        WebElement customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        String customerName = selectRandomFromComboBox(customerComboBox);
        int originalOrderElements = countVisibleGridDataRows(createOrderGridXpath);

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);

        applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        resetFilter(orderElementGridXpath);

        navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        selectElementByTextFromComboBox(customerComboBox, customerName);
        //selectRandomFromComboBox(customerComboBox);
        Thread.sleep(200);
        findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(originalOrderElements + 3, countVisibleGridDataRows(createOrderGridXpath));

        selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
        selectRandomFromComboBox(findVisibleElementWithXpath(currencyComboBoxXpath));
        selectRandomFromComboBox(findVisibleElementWithXpath(paymentMethodComboBoxXpath));


        mockDatabaseNotAvailable(spyDataSource, 1);

        findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        checkNotificationText("Order saving failed: Internal server error");

        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        assertEquals(originalOrderNumber, countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    public void moreThanOneOrderExistsForCustomerEditOne() throws InterruptedException {
        init();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = countVisibleGridDataRows(createOrderGridXpath);

        for(int i = 0; i < 2; i++){
            navigateMenu(mainMenu, subMenu);
            Thread.sleep(100);

            WebElement customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
            String customerName = selectRandomFromComboBox(customerComboBox);
            int originalOrderElements = countVisibleGridDataRows(createOrderGridXpath);

            navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
            Thread.sleep(100);
            findVisibleElementWithXpath(orderElementGridXpath);
            LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

            String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
            sameUser.put("Customer", customerName);
            sameUser.put("Supplier", "");

            orderElementCrudTestingUtil.createTest(sameUser, "", true);
            orderElementCrudTestingUtil.createTest(sameUser, "", true);
            orderElementCrudTestingUtil.createTest(sameUser, "", true);
            orderElementCrudTestingUtil.createTest(sameUser, "", true);
            orderElementCrudTestingUtil.createTest(sameUser, "", true);

            applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
            orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
            orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
            resetFilter(orderElementGridXpath);

            navigateMenu(mainMenu, subMenu);
            Thread.sleep(100);

            customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
            selectElementByTextFromComboBox(customerComboBox, customerName);
            //selectRandomFromComboBox(customerComboBox);
            Thread.sleep(200);
            findVisibleElementWithXpath(createOrderGridXpath);
            assertEquals(originalOrderElements + 3, countVisibleGridDataRows(createOrderGridXpath)); //TODO valamiért ez mindig 0-ra jön ki éles futáskor.

            selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
            selectRandomFromComboBox(findVisibleElementWithXpath(currencyComboBoxXpath));
            selectRandomFromComboBox(findVisibleElementWithXpath(paymentMethodComboBoxXpath));

            findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
            checkNotificationContainsTexts("Order saved:");
        }
//        assertEquals(countVisibleGridDataRows(createOrderGridXpath), originalOrderNumber + 2);
        updateOrder();

    }


    @Test
    public void noCustomerSelectedButShowPreviouslyEnabledThanGridWillBeIsEmpty() throws InterruptedException {
        init();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_CREATE_SUBMENU);
        Thread.sleep(100);
        assertEquals(countVisibleGridDataRows(orderElementGridXpath), 0);
        setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
        assertEquals(countVisibleGridDataRows(orderElementGridXpath), 0);
    }

    @Test
    public void deselectShowPreviouslyChangesGridSelectionMode() throws InterruptedException {
        init();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_CREATE_SUBMENU);
        Thread.sleep(100);
        assertEquals(countVisibleGridDataRows(orderElementGridXpath), 0);

        WebElement customerComboBox = findVisibleElementWithXpath(customerComboBoxXpath);
        int originalOrderElements = countVisibleGridDataRows(createOrderGridXpath);
        selectRandomFromComboBox(customerComboBox);

        setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
        softAssert.assertEquals(isInMultiSelectMode(createOrderGridXpath), true);
        setCheckboxStatus(previouslyOrderedCheckboxXpath, false);
        softAssert.assertEquals(isInMultiSelectMode(createOrderGridXpath), false);
    }

    private void checkField(String fieldXpath, String errorMessage){
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(fieldXpath)), false, "A megadott mező enabled, pedig disabled kell: " + findVisibleElementWithXpath(fieldXpath).getText());
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(orderCreateOrderButtonXpath)), false);
        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(fieldXpath)), errorMessage);
    }
}

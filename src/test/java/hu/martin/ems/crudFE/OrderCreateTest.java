package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import org.openqa.selenium.WebElement;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.LinkedHashMap;

import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
public class OrderCreateTest extends BaseCrudTest {

    public static final String customerComboBoxXpath = contentXpath + "/vaadin-form-layout[1]/vaadin-combo-box";
    public static final String currencyComboBoxXpath = contentXpath + "/vaadin-form-layout[2]/vaadin-combo-box[1]";
    public static final String paymentMethodComboBoxXpath = contentXpath + "/vaadin-form-layout[2]/vaadin-combo-box[2]";
    private static final String orderElementShowDeletedXpath = OrderElementCrudTest.showDeletedCheckBoxXpath;
    private static final String orderElementGridXpath = OrderElementCrudTest.gridXpath;
    private static final String orderElementCreateButtonXpath = OrderElementCrudTest.createButtonXpath;

    private static final String orderXpath = contentXpath + "/vaadin-grid";
    public static final String orderCreateOrderButtonXpath = contentXpath + "/vaadin-form-layout[2]/vaadin-button";

    public static final String previouslyOrderedCheckboxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";

    public static final SoftAssert softAssert = new SoftAssert();

    private static CrudTestingUtil crudTestingUtil;
    private static CrudTestingUtil orderElementCrudTestingUtil;

    public static final String createOrderGridXpath = contentXpath + "/vaadin-grid";


    private static final String mainMenu = UIXpaths.ORDERS_MENU;
    private static final String subMenu = UIXpaths.ORDER_CREATE_SUBMENU;


    private GridTestingUtil gridTestingUtil;

    

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(driver);
        init();
    }

    private void init(){
        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "Order", null, createOrderGridXpath, null);
        orderElementCrudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "OrderElement", orderElementShowDeletedXpath, orderElementGridXpath, orderElementCreateButtonXpath);
    }

    @Test
    
    public void createOrderTest() throws InterruptedException {
        createOrder();
    }

    @Test
    
    public void customerNotSelectedShowPreviouslyGridIsEmptyTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);
        int originalRows = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        WebElement previously = gridTestingUtil.findVisibleElementWithXpath(previouslyOrderedCheckboxXpath);
        gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
        gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, false);
        assertEquals(originalRows, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
    }

    public void createOrder() throws InterruptedException {
        createOrder(null, true);
    }

    public void createOrder(String notificationText, Boolean requiredSuccess) throws InterruptedException {
        init();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        WebElement customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
        String customerName = gridTestingUtil.selectRandomFromComboBox(customerComboBox);
        int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);

        gridTestingUtil.applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        gridTestingUtil.resetFilter(orderElementGridXpath);

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
        gridTestingUtil.selectElementByTextFromComboBox(customerComboBox, customerName);
        //gridTestingUtil.selectRandomFromComboBox(customerComboBox);
        Thread.sleep(200);
        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(originalOrderElements + 3, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));

        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        if(notificationText == null){
            gridTestingUtil.checkNotificationContainsTexts("Order saved:");
        }
        else{
            gridTestingUtil.checkNotificationText(notificationText);
        }

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        if(requiredSuccess){
            assertEquals(originalOrderNumber + 1, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
        }
        else{
            assertEquals(originalOrderNumber, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
        }
    }

    @Test
    
    public void nullResponseFromServiceWhenModify() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 21);
//         Mockito.doReturn(null).when(spyOrderService).update(any(Order.class));
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        updateOrder("Order modifying failed: Internal Server Error", false);
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    
    public void databaseNotAvailableWhenCreate() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyOrderService).save(any(Order.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 95);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        createOrder("Order saving failed: Internal Server Error", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    
    public void gettingCustomersFailedTest() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 2);
//        Mockito.doReturn(null).when(spyCustomerService).findAll(false); //Controllerben opcionális paraméterként jön.
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        checkField(customerComboBoxXpath, "Error happened while getting customers");
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    
    public void getOrderElementsByCustomerFailedTest() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyOrderElementService).getByCustomer(any(Long.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath));
        Thread.sleep(100);
        gridTestingUtil.checkNotificationText("Error happened while getting order elements to the customer");
        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    
    public void getPendingCodeStoreFailedTest() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).findByName("Pending"); //ApiClint-ben getAllByName("Pending");
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 93);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        createOrder("Error happened while getting \"Pending\" status", false);
    }

    @Test
    
    public void getPaymentTypesFailedTest() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.PAYMENT_TYPES_CODESTORE_ID); //id:7
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        checkField(paymentMethodComboBoxXpath, "Error happened while getting payment methods");
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    
    public void getCurrencyTypesFailedTest() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyCodeStoreService).getChildren(StaticDatas.CURRENCIES_CODESTORE_ID); //id 1
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        checkField(currencyComboBoxXpath, "Error happened while getting currencies");
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    
    public void updateOrder() throws InterruptedException {
        updateOrder(null, true);
    }

    public void updateOrder(String notificationText, Boolean requiredSuccess) throws InterruptedException {
        init();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        int originalOrderNumber = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);
        if(originalOrderNumber == 0){
            createOrder();
        }

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        ElementLocation randomLocation = gridTestingUtil.getRandomLocationFromGrid(createOrderGridXpath);
        gridTestingUtil.goToPageInPaginatedGrid(createOrderGridXpath, randomLocation.getPageNumber());
        String[] originalData = gridTestingUtil.getDataFromRowLocation(createOrderGridXpath, randomLocation);
        Thread.sleep(200);
        gridTestingUtil.applyFilter(createOrderGridXpath, originalData);
        assertEquals(1, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
        gridTestingUtil.resetFilter(createOrderGridXpath);

        gridTestingUtil.getModifyButton(createOrderGridXpath, randomLocation.getRowIndex()).click();

        Thread.sleep(200);
        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath, 5000);

        Thread.sleep(200);
        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 1);
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        if(notificationText == null){
            gridTestingUtil.checkNotificationContainsTexts("Order updated:");
        }
        else{
            gridTestingUtil.checkNotificationText(notificationText);
        }

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        if(requiredSuccess){
            assertEquals(originalOrderNumber, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
            gridTestingUtil.applyFilter(createOrderGridXpath, originalData);
            assertEquals(0, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
            gridTestingUtil.resetFilter(createOrderGridXpath);
        }
        else{
            assertEquals(originalOrderNumber, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
            gridTestingUtil.applyFilter(createOrderGridXpath, originalData);
            assertEquals(1, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
            gridTestingUtil.resetFilter(createOrderGridXpath);
        }
    }

    @Test
    
    public void getOrderElementsByOrderIdFailedWhenSaveOrder() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 90);
//        Mockito.doReturn(null).when(spyOrderService).save(any(Order.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyOrderApiClient).save(any(Order.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyOrderApiClient).getOrderElements(any(Long.class));
        createOrder("Order saving failed: Internal Server Error", false);
    }

    @Test
    
    public void noneSelectedFromTheOrderCreationGrid() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        init();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        WebElement customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
        String customerName = gridTestingUtil.selectRandomFromComboBox(customerComboBox);
        int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);

        gridTestingUtil.applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        gridTestingUtil.resetFilter(orderElementGridXpath);

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);

        gridTestingUtil.selectElementByTextFromComboBox(customerComboBox, customerName);
        //gridTestingUtil.selectRandomFromComboBox(customerComboBox);
        Thread.sleep(200);
        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath), originalOrderElements + 3);

        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 0);
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        gridTestingUtil.checkNotificationText("Order must contains at least one order element!");

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        assertEquals(originalOrderNumber, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    
    public void databaseUnavailableWhenGettingAllByCustomer() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        init();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        WebElement customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
        String customerName = gridTestingUtil.selectRandomFromComboBox(customerComboBox);
        int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);

        gridTestingUtil.applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        gridTestingUtil.resetFilter(orderElementGridXpath);

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);

        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(this, spyDataSource, 0);

        gridTestingUtil.selectElementByTextFromComboBox(customerComboBox, customerName);
        //gridTestingUtil.selectRandomFromComboBox(customerComboBox);
        Thread.sleep(200);
        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath), 0);

        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));

        gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        gridTestingUtil.checkNotificationText("Error happened while getting order elements to the customer");

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        assertEquals(originalOrderNumber, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        init();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        WebElement customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
        String customerName = gridTestingUtil.selectRandomFromComboBox(customerComboBox);
        int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
        Thread.sleep(100);

        gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
        LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

        String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
        sameUser.put("Customer", customerName);
        sameUser.put("Supplier", "");
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);
        orderElementCrudTestingUtil.createTest(sameUser, "", true);

        gridTestingUtil.applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
        gridTestingUtil.resetFilter(orderElementGridXpath);

        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(100);

        customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
        gridTestingUtil.selectElementByTextFromComboBox(customerComboBox, customerName);
        //gridTestingUtil.selectRandomFromComboBox(customerComboBox);
        Thread.sleep(200);
        gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
        assertEquals(originalOrderElements + 3, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));

        gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
        gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));


        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(this, spyDataSource, 1);

        gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
        gridTestingUtil.checkNotificationText("Order saving failed: Internal Server Error");

        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);
        assertEquals(originalOrderNumber, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));
    }

    @Test
    
    public void moreThanOneOrderExistsForCustomerEditOne() throws InterruptedException {
        init();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_SUBMENU);
        Thread.sleep(100);

        int originalOrderNumber = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

        for(int i = 0; i < 2; i++){
            gridTestingUtil.navigateMenu(mainMenu, subMenu);
            Thread.sleep(100);

            WebElement customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
            String customerName = gridTestingUtil.selectRandomFromComboBox(customerComboBox);
            int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);

            gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_ELEMENT_SUBMENU);
            Thread.sleep(100);
            gridTestingUtil.findVisibleElementWithXpath(orderElementGridXpath);
            LinkedHashMap<String, String> sameUser = new LinkedHashMap<>();

            String[] orderElementGridCustomerFilter = new String[]{"", "", "null", "", "", "", "", customerName};
            sameUser.put("Customer", customerName);
            sameUser.put("Supplier", "");

            orderElementCrudTestingUtil.createTest(sameUser, "", true);
            orderElementCrudTestingUtil.createTest(sameUser, "", true);
            orderElementCrudTestingUtil.createTest(sameUser, "", true);
            orderElementCrudTestingUtil.createTest(sameUser, "", true);
            orderElementCrudTestingUtil.createTest(sameUser, "", true);

            gridTestingUtil.applyFilter(orderElementGridXpath, orderElementGridCustomerFilter);
            orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
            orderElementCrudTestingUtil.deleteTest(null, true, orderElementGridCustomerFilter);
            gridTestingUtil.resetFilter(orderElementGridXpath);

            gridTestingUtil.navigateMenu(mainMenu, subMenu);
            Thread.sleep(100);

            customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
            gridTestingUtil.selectElementByTextFromComboBox(customerComboBox, customerName);
            //gridTestingUtil.selectRandomFromComboBox(customerComboBox);
            Thread.sleep(200);
            gridTestingUtil.findVisibleElementWithXpath(createOrderGridXpath);
            assertEquals(originalOrderElements + 3, gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath));

            gridTestingUtil.selectMultipleElementsFromMultibleSelectionGrid(createOrderGridXpath, 2);
            gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(currencyComboBoxXpath));
            gridTestingUtil.selectRandomFromComboBox(gridTestingUtil.findVisibleElementWithXpath(paymentMethodComboBoxXpath));

            gridTestingUtil.findClickableElementWithXpathWithWaiting(orderCreateOrderButtonXpath).click();
            gridTestingUtil.checkNotificationContainsTexts("Order saved:");
        }
//        assertEquals(gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath), originalOrderNumber + 2);
        updateOrder();

    }


    @Test
    
    public void noCustomerSelectedButShowPreviouslyEnabledThanGridWillBeIsEmpty() throws InterruptedException {
        init();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_CREATE_SUBMENU);
        Thread.sleep(100);
        assertEquals(gridTestingUtil.countVisibleGridDataRows(orderElementGridXpath), 0);
        gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
        assertEquals(gridTestingUtil.countVisibleGridDataRows(orderElementGridXpath), 0);
    }

    @Test
    
    public void deselectShowPreviouslyChangesGridSelectionMode() throws InterruptedException {
        init();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_CREATE_SUBMENU);
        Thread.sleep(100);
        assertEquals(gridTestingUtil.countVisibleGridDataRows(orderElementGridXpath), 0);

        WebElement customerComboBox = gridTestingUtil.findVisibleElementWithXpath(customerComboBoxXpath);
        int originalOrderElements = gridTestingUtil.countVisibleGridDataRows(createOrderGridXpath);
        gridTestingUtil.selectRandomFromComboBox(customerComboBox);

        gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, true);
        softAssert.assertEquals(gridTestingUtil.isInMultiSelectMode(createOrderGridXpath), true);
        gridTestingUtil.setCheckboxStatus(previouslyOrderedCheckboxXpath, false);
        softAssert.assertEquals(gridTestingUtil.isInMultiSelectMode(createOrderGridXpath), false);
    }

    private void checkField(String fieldXpath, String errorMessage){
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(fieldXpath)), false, "A megadott mező enabled, pedig disabled kell: " + gridTestingUtil.findVisibleElementWithXpath(fieldXpath).getText());
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(orderCreateOrderButtonXpath)), false);
        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(fieldXpath)), errorMessage);
    }
}

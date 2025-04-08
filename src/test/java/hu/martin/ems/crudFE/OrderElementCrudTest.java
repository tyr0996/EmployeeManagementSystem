package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.OrderCreatePage;
import hu.martin.ems.pages.OrderElementPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.OrderElementSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderElementCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    public static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    public static final String gridXpath = contentXpath + "/vaadin-grid";
    public static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";

    private static final String mainMenu = UIXpaths.ORDERS_MENU;
    private static final String subMenu = UIXpaths.ORDER_ELEMENT_SUBMENU;

    private GridTestingUtil gridTestingUtil;

    

    private OrderCreateTest orderCreateTest;

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(driver);
        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "OrderElement", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
        orderCreateTest = new OrderCreateTest();
    }

    @Test
    public void orderElementCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        DoCreateTestData testResult = page.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement saved: ");
    }

    @Test
    public void orderElementReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);





//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        List<String[]> allFullLines = page.getGrid().getAllFullLines(true);
        List<String[]> allNonOrderedLines = page.getGrid().getAllLackingLines(true);
        while(allFullLines.size() == 0){
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_SUBMENU);
            OrderCreatePage orderCreatePage = new OrderCreatePage(driver, port);
            orderCreatePage.performCreate(null);
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
            allFullLines = page.getGrid().getAllFullLines(true);

//            orderCreateTest.createOrder();
//            gridTestingUtil.navigateMenu(mainMenu, subMenu);
//            allFullLines = crudTestingUtil.getAllDataLinesFull();
        }

        while(allNonOrderedLines.size() == 0) {
            page.performCreate(null);
            allNonOrderedLines = page.getGrid().getAllLackingLines(true);
//            orderElementCreateTest();
//            gridTestingUtil.navigateMenu(mainMenu, subMenu); //TODO: itt volt egy ilyen navigate, amit nem értek, hogy miért kell.
//            allNonOrderedLines = crudTestingUtil.getAllDataLinesWithEmpty();
        }

        Collections.shuffle(allFullLines);
        Collections.shuffle(allNonOrderedLines);

        SoftAssert sa = new SoftAssert();

//        crudTestingUtil.readTest(allFullLines.get(0), null, false, null);
        page.getGrid().applyFilter(allFullLines.get(0));
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        page.getGrid().resetFilter();

        page.getGrid().applyFilter(allNonOrderedLines.get(0));
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        page.getGrid().resetFilter();

        sa.assertAll();

//        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
//        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
//        assertNull(testResult.getNotificationWhenPerform());
//        crudTestingUtil.readTest(allNonOrderedLines.get(0), null, false, null);
    }

    @Test
    public void orderElementDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);

        DoDeleteTestData testResult = page.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement deleted: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();


//
//
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.deleteTest();
    }


    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);

        DoDeleteFailedTestData testResult = page.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void orderElementUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);

        DoUpdateTestData testResult = page.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement updated: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

    @Test
    public void orderElementRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        DoRestoreTestData testResult = page.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement restored: ");

        page = new OrderElementPage(driver, port);
        page.getGrid().applyFilter(testResult.getResult().getRestoredData());
        page.getGrid().waitForRefresh();
        assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        page.getGrid().resetFilter();
//
//
//
//
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.restoreTest();
    }

    @Test
    public void orderElementPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);

        DoPermanentlyDeleteTestData testResult = page.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        page.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

//    //@Test
//    public void extraFilterInvalidValue() throws InterruptedException {
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        NotificationCheck nc = new NotificationCheck();
//        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
//        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
//    }


    @Test
    public void findAllOrderElementWithDeletedFailedButWithoutIsSuccess() throws SQLException {
        SoftAssert sa = new SoftAssert();
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 3);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        int elements = page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox());
        sa.assertNotEquals(elements, 0);
        page.getShowDeletedCheckBox().setStatus(true);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "Getting order elements failed");
        notification.close();
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), elements);

        sa.assertAll();
    }

    @Test
    public void findAllOrderElementWithAndWithoutFailed() throws SQLException, InterruptedException {
        SoftAssert sa = new SoftAssert();
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 2);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        VaadinNotificationComponent notification_1 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification_1.getText(), "Error happened while getting order elements");
        notification_1.close();

        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        VaadinNotificationComponent notification_2 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification_2.getText(), "Getting order elements failed");
        notification_2.close();

        sa.assertAll();
    }

    @Test
    public void findAllSuppliersFailed() throws SQLException, InterruptedException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 5);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        page.getCreateButton().click();
        OrderElementSaveOrUpdateDialog dialog = new OrderElementSaveOrUpdateDialog(driver);
        dialog.initWebElements();assertEquals(dialog.getFailedComponents().size(), 1);
        assertEquals(dialog.getFailedComponents().get(0).getErrorMessage(), "Error happened while getting suppliers");
        assertEquals(dialog.getFailedComponents().get(0).getFieldTitle(), "Supplier");

////        Mockito.doReturn(null).when(spySupplierService).findAll(false);//Controllerben alapértelmezett
//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 5);
//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
//        failedFieldData.put("Supplier", "");
//        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
    }

    @Test
    public void findAllCustomersFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 4);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        page.getCreateButton().click();
        OrderElementSaveOrUpdateDialog dialog = new OrderElementSaveOrUpdateDialog(driver);
        dialog.initWebElements();assertEquals(dialog.getFailedComponents().size(), 1);
        assertEquals(dialog.getFailedComponents().get(0).getErrorMessage(), "Error happened while getting customers");
        assertEquals(dialog.getFailedComponents().get(0).getFieldTitle(), "Customer");
    }

    @Test
    public void findAllProductsFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 3);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        page.getCreateButton().click();
        OrderElementSaveOrUpdateDialog dialog = new OrderElementSaveOrUpdateDialog(driver);
        dialog.initWebElements();assertEquals(dialog.getFailedComponents().size(), 1);
        assertEquals(dialog.getFailedComponents().get(0).getErrorMessage(), "Error happened while getting products");
        assertEquals(dialog.getFailedComponents().get(0).getFieldTitle(), "Product");
    }

    @Test
    public void databaseUnavailableWhenSaving() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        DoCreateFailedTestData testResult = page.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());



//        gridTestingUtil.loginWith(driver, port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }

    @Test
    public void databaseUnavailableWhenModifying() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        DoUpdateFailedTestData testResult = page.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("OrderElement modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }
}
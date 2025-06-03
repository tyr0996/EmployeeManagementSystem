package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.OrderCreateToCustomerPage;
import hu.martin.ems.pages.OrderElementPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.OrderElementSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderElementCrudTest extends BaseCrudTest {

    public static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    public static final String gridXpath = contentXpath + "/vaadin-grid";
    public static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";

    @Test
    public void createOrderElementForBothCustomerAndSupplier() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Customer", "Erdei Róbert");
        withData.put("Supplier", "Szállító1");

        OrderElementPage page = new OrderElementPage(driver, port);
        DoCreateTestData testResult = page.doCreateTest(withData);
        String[] filter = new String[]{"", "", "", "", "", "", "", "(S) Szállító1, (C) Erdei Róbert"};
        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement saved: ");
        page.getGrid().applyFilter(filter);
        page.getGrid().waitForRefresh();
        assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void orderElementCreateTestForCustomer() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Customer", "Erdei Róbert");
        DoCreateTestData testResult = page.doCreateTest(withData);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement saved: ");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void orderElementCreateTestForSupplier() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Supplier", "Szállító1");
        DoCreateTestData testResult = page.doCreateTest(withData);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement saved: ");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void orderElementReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);

        List<String[]> allFullLines = page.getGrid().getAllFullLines(true);
        List<String[]> allNonOrderedLines = page.getGrid().getAllLackingLines(true);
        while (allFullLines.size() == 0) {
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_CREATE_TO_CUSTOMER_SUBMENU);
            OrderCreateToCustomerPage orderCreateToCustomerPage = new OrderCreateToCustomerPage(driver, port);
            orderCreateToCustomerPage.performCreate(null);
            loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);
            allFullLines = page.getGrid().getAllFullLines(true);
        }

        while (allNonOrderedLines.size() == 0) {
            page.performCreate(null);
            allNonOrderedLines = page.getGrid().getAllLackingLines(true);
        }

        Collections.shuffle(allFullLines);
        Collections.shuffle(allNonOrderedLines);

        SoftAssert sa = new SoftAssert();

        page.getGrid().applyFilter(allFullLines.get(0));
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        page.getGrid().resetFilter();

        page.getGrid().applyFilter(allNonOrderedLines.get(0));
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        page.getGrid().resetFilter();

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    @Video
    public void orderElementDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);

        DoDeleteTestData testResult = page.doDeleteTest();

        SoftAssert sa = new SoftAssert();

        sa.assertEquals((int) testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        sa.assertEquals((int) testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        sa.assertTrue(testResult.getNotificationWhenPerform().contains("OrderElement deleted: "));

        page.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        page.getGrid().resetFilter();
        sa.assertNull(VaadinNotificationComponent.hasNotification(driver));

        sa.assertAll();
    }


    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);

        DoDeleteFailedTestData testResult = page.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Database error");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void orderElementUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
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
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void orderElementRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        DoRestoreTestData testResult = page.doRestoreTest();
        SoftAssert sa = new SoftAssert();

        sa.assertEquals((int) testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        sa.assertEquals((int) testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        sa.assertTrue(testResult.getNotificationWhenPerform().contains("OrderElement restored: "));

        page = new OrderElementPage(driver, port);
        page.getGrid().applyFilter(testResult.getResult().getRestoredData());
        page.getGrid().waitForRefresh();
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        page.getGrid().resetFilter();

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    @Video
    public void orderElementPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);

        SoftAssert sa = new SoftAssert();

        DoPermanentlyDeleteTestData testResult = page.doPermanentlyDeleteTest();
        sa.assertTrue(testResult.getNotificationWhenPerform().contains("OrderElement permanently deleted: "));


        sa.assertEquals((int) testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        sa.assertEquals((int) testResult.getNonDeletedRowNumberAfterMethod(), (int) testResult.getOriginalNonDeletedRowNumber());
        page.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        sa.assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        sa.assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        sa.assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
        sa.assertNull(VaadinNotificationComponent.hasNotification(driver));

        sa.assertAll();
    }

    @Test
    public void findAllOrderElementWithDeletedFailedButWithoutIsSuccess() throws SQLException {
        SoftAssert sa = new SoftAssert();
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(3, 4));
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        int elements = page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox());
        sa.assertNotEquals(elements, 0);
        page.getShowDeletedCheckBox().setStatus(true);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "Getting order elements failed");
        notification.close();
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        VaadinNotificationComponent notification1 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification1.getText(), "Getting order elements failed");
        notification1.close();
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), elements);


        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void findAllOrderElementWithAndWithoutFailed() throws SQLException {
        SoftAssert sa = new SoftAssert();
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(0, 1, 2));
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);


        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "Getting order elements failed");
        notification.close();

        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        VaadinNotificationComponent notification2 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification2.getText(), "Getting order elements failed");
        notification2.close();

        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        VaadinNotificationComponent notification3 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification3.getText(), "Getting order elements failed");
        notification3.close();

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void findAllSuppliersFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 5);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        page.getCreateButton().click();
        OrderElementSaveOrUpdateDialog dialog = new OrderElementSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        assertEquals(dialog.getFailedComponents().size(), 1);
        assertEquals(dialog.getFailedComponents().get(0).getErrorMessage(), "EmsError happened while getting suppliers");
        assertEquals(dialog.getFailedComponents().get(0).getFieldTitle(), "Supplier");
        dialog.close();
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened while getting suppliers");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void findAllCustomersFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 4);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        page.getCreateButton().click();
        OrderElementSaveOrUpdateDialog dialog = new OrderElementSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        assertEquals(dialog.getFailedComponents().size(), 1);
        assertEquals(dialog.getFailedComponents().get(0).getErrorMessage(), "EmsError happened while getting customers");
        assertEquals(dialog.getFailedComponents().get(0).getFieldTitle(), "Customer");
        dialog.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void findAllProductsFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 3);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        page.getCreateButton().click();
        OrderElementSaveOrUpdateDialog dialog = new OrderElementSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        assertEquals(dialog.getFailedComponents().size(), 1);
        assertEquals(dialog.getFailedComponents().get(0).getErrorMessage(), "EmsError happened while getting products");
        assertEquals(dialog.getFailedComponents().get(0).getFieldTitle(), "Product");
        dialog.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseUnavailableWhenSaving() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        DoCreateFailedTestData testResult = page.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("OrderElement saving failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseUnavailableWhenModifying() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ORDERS_MENU, SideMenu.ORDER_ELEMENT_SUBMENU);

        OrderElementPage page = new OrderElementPage(driver, port);
        DoUpdateFailedTestData testResult = page.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("OrderElement modifying failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }
}
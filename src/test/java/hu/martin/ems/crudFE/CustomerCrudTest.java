package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.CustomerPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.component.saveOrUpdateDialog.CustomerSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class CustomerCrudTest extends BaseCrudTest {
    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        DoDeleteFailedTestData testResult = customerPage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void customerCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        DoCreateTestData testResult = customerPage.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Customer saved: ");
    }

    @Test
    public void customerReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        DoReadTestData testResult = customerPage.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
    }

    @Test
    public void customerDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        DoDeleteTestData testResult = customerPage.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Customer deleted: ");

        customerPage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, customerPage.getGrid().getTotalDeletedRowNumber(customerPage.getShowDeletedCheckBox()));
        assertEquals(0, customerPage.getGrid().getTotalNonDeletedRowNumber(customerPage.getShowDeletedCheckBox()));
        customerPage.getGrid().resetFilter();
    }

    @Test
    public void customerUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        DoUpdateTestData testResult = customerPage.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Customer updated: ");

        customerPage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, customerPage.getGrid().getTotalDeletedRowNumber(customerPage.getShowDeletedCheckBox()));
        assertEquals(0, customerPage.getGrid().getTotalNonDeletedRowNumber(customerPage.getShowDeletedCheckBox()));
        customerPage.getGrid().resetFilter();
    }

    @Test
    public void customerRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        DoRestoreTestData testResult = customerPage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Customer restored: ");

        customerPage = new CustomerPage(driver, port);
        customerPage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        customerPage.getGrid().waitForRefresh();
        assertEquals(customerPage.getGrid().getTotalDeletedRowNumber(customerPage.getShowDeletedCheckBox()), 0);
        assertEquals(customerPage.getGrid().getTotalNonDeletedRowNumber(customerPage.getShowDeletedCheckBox()), 1);
        customerPage.getGrid().resetFilter();
    }

    @Test
    public void customerPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        DoPermanentlyDeleteTestData testResult = customerPage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Customer permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        customerPage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, customerPage.getGrid().getTotalDeletedRowNumber(customerPage.getShowDeletedCheckBox()));
        assertEquals(0, customerPage.getGrid().getTotalNonDeletedRowNumber(customerPage.getShowDeletedCheckBox()));
        customerPage.getGrid().resetFilter();
    }

    @Test
    public void nullResponseFromServiceWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        DoUpdateFailedTestData testResult = customerPage.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Customer modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        DoCreateFailedTestData testResult = customerPage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Customer saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void unexpectedResponseCodeWhenFindAllCustomer() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 0);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Error happened while getting customers");
        notification.close();

        CustomerPage customerPage = new CustomerPage(driver, port);
        assertEquals(customerPage.getGrid().getTotalNonDeletedRowNumber(customerPage.getShowDeletedCheckBox()), 0);
        assertEquals(customerPage.getGrid().getTotalDeletedRowNumber(customerPage.getShowDeletedCheckBox()), 0);
    }

    @Test
    public void unexpectedResponseCodeWhenFindAllAddress() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "admin", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.CUSTOMER_SUBMENU);

        CustomerPage customerPage = new CustomerPage(driver, port);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        customerPage.getCreateButton().click();

        CustomerSaveOrUpdateDialog dialog = new CustomerSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "Error happened while getting addresses");
    }
}

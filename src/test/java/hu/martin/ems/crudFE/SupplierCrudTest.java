package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.SupplierPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.SupplierSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SupplierCrudTest extends BaseCrudTest {
    @Test
    public void closeCreateDialogTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        supplierPage.getCreateButton().click();
        SupplierSaveOrUpdateDialog dialog = new SupplierSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        dialog.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void supplierCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoCreateTestData testResult = supplierPage.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier saved: ");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoDeleteFailedTestData testResult = supplierPage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Database error");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void supplierReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoReadTestData testResult = supplierPage.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void supplierDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoDeleteTestData testResult = supplierPage.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier deleted: ");

        supplierPage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        assertEquals(0, supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        supplierPage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void supplierUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoUpdateTestData testResult = supplierPage.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier updated: ");

        supplierPage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        assertEquals(0, supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        supplierPage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void supplierRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoRestoreTestData testResult = supplierPage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier restored: ");

        supplierPage = new SupplierPage(driver, port);
        supplierPage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        supplierPage.getGrid().waitForRefresh();
        assertEquals(supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()), 0);
        assertEquals(supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()), 1);
        supplierPage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void supplierPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoPermanentlyDeleteTestData testResult = supplierPage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        supplierPage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        assertEquals(0, supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()));
        supplierPage.getGrid().resetFilter();


        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoUpdateFailedTestData testResult = supplierPage.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Supplier modifying failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test

    public void nullResponseFromServiceWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        DoCreateFailedTestData testResult = supplierPage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Supplier saving failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void unexpectedResponseCodeWhenGettingAllSuppliers() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(0, 1, 2, 3, 4));
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SoftAssert sa = new SoftAssert();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "EmsError happened while getting suppliers: Database error");
        notification.close();

        SupplierPage supplierPage = new SupplierPage(driver, port);
        sa.assertEquals(supplierPage.getGrid().getTotalDeletedRowNumber(supplierPage.getShowDeletedCheckBox()), 0);
        VaadinNotificationComponent notification1 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification1.getText(), "EmsError happened while getting suppliers: Database error");
        notification1.close();

        sa.assertEquals(supplierPage.getGrid().getTotalNonDeletedRowNumber(supplierPage.getShowDeletedCheckBox()), 0);
        VaadinNotificationComponent notification2 = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification2.getText(), "EmsError happened while getting suppliers: Database error");
        notification2.close();

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test

    public void unexpectedResponseCodeWhenGettingAddresses() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.SUPPLIER_SUBMENU);

        SupplierPage supplierPage = new SupplierPage(driver, port);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        supplierPage.getCreateButton().click();

        SupplierSaveOrUpdateDialog dialog = new SupplierSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "EmsError happened while getting addresses");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }
}

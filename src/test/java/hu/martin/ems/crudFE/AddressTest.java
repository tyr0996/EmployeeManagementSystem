package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.pages.AddressPage;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.InternalErrorNotification;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.AddressSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AddressTest extends BaseCrudTest {

    @Test
    public void addressCreateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoCreateTestData testResult = addressPage.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Address saved: ");
    }

    @Test
    public void addressReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoReadTestData testResult = addressPage.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
    }

    @Test
    public void addressDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoDeleteTestData testResult = addressPage.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Address deleted: ");

        addressPage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, addressPage.getGrid().getTotalDeletedRowNumber(addressPage.getShowDeletedCheckBox()));
        assertEquals(0, addressPage.getGrid().getTotalNonDeletedRowNumber(addressPage.getShowDeletedCheckBox()));
        addressPage.getGrid().resetFilter();
    }

    @Test
    public void addressUpdateTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoUpdateTestData testResult = addressPage.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Address updated: ");

        addressPage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, addressPage.getGrid().getTotalDeletedRowNumber(addressPage.getShowDeletedCheckBox()));
        assertEquals(0, addressPage.getGrid().getTotalNonDeletedRowNumber(addressPage.getShowDeletedCheckBox()));
        addressPage.getGrid().resetFilter();
    }

    @Test
    public void addressRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoRestoreTestData testResult = addressPage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Address restored: ");

        addressPage = new AddressPage(driver, port);
        addressPage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        addressPage.getGrid().waitForRefresh();
        assertEquals(addressPage.getGrid().getTotalDeletedRowNumber(addressPage.getShowDeletedCheckBox()), 0);
        assertEquals(addressPage.getGrid().getTotalNonDeletedRowNumber(addressPage.getShowDeletedCheckBox()), 1);
        addressPage.getGrid().resetFilter();

    }

    @Test
    public void addressPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoPermanentlyDeleteTestData testResult = addressPage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Address permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        addressPage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, addressPage.getGrid().getTotalDeletedRowNumber(addressPage.getShowDeletedCheckBox()));
        assertEquals(0, addressPage.getGrid().getTotalNonDeletedRowNumber(addressPage.getShowDeletedCheckBox()));
        addressPage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
    }

    @Test
    public void addressPermanentlyDeleteClearTableFailedTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoPermanentlyDeleteTestData testResult = addressPage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Address permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        addressPage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, addressPage.getGrid().getTotalDeletedRowNumber(addressPage.getShowDeletedCheckBox()));
        assertEquals(0, addressPage.getGrid().getTotalNonDeletedRowNumber(addressPage.getShowDeletedCheckBox()));
        addressPage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Internal Server Error");
        notificationComponent.close();
    }

    @Test
    public void gettingCountriesFailed() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 1);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        addressPage.getCreateButton().click();

        AddressSaveOrUpdateDialog dialog = new AddressSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "Error happened while getting countries");
        dialog.close();
    }

    @Test
    public void gettingCitiesFailed() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 2);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        addressPage.getCreateButton().click();

        AddressSaveOrUpdateDialog dialog = new AddressSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "Error happened while getting cities");
        dialog.close();
    }

    @Test
    public void gettingStreetTypesFailed() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 3);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        addressPage.getCreateButton().click();

        AddressSaveOrUpdateDialog dialog = new AddressSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "Error happened while getting street types");
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoDeleteFailedTestData testResult = addressPage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhilePermanentlyDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoPermanentlyDeleteFailedTestData testResult = addressPage.doDatabaseNotAvailableWhenPermanentlyDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Address permanently deletion failed: Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoUpdateFailedTestData testResult = addressPage.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Address modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        AddressPage addressPage = new AddressPage(driver, port);
        DoCreateFailedTestData testResult = addressPage.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Address saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void databaseNotAvailableWhenOpenPage() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 0);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);

        SoftAssert sa = new SoftAssert();
        AddressPage addressPage = new AddressPage(driver, port);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "Error happened while getting addresses");
        sa.assertEquals(addressPage.getGrid().getTotalDeletedRowNumber(addressPage.getShowDeletedCheckBox()), 0);
        sa.assertEquals(addressPage.getGrid().getTotalNonDeletedRowNumber(addressPage.getShowDeletedCheckBox()), 0);

        sa.assertAll();
    }

    @Test
    public void oneOfTheIconsIOExceptionTest() throws IOException{
        doThrow(IOException.class).when(spyIconProvider).readAllBytes(any(Path.class));
        EmptyLoggedInVaadinPage loggedInPage = (EmptyLoggedInVaadinPage)
                LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADDRESS_SUBMENU);
        InternalErrorNotification notification = new InternalErrorNotification(driver);
        SoftAssert sa = new SoftAssert();
        sa.assertEquals(notification.getCaption().getText(), "Internal error");
        sa.assertEquals(notification.getMessage().getText(), "Please notify the administrator. Take note of any unsaved data, and click here or press ESC to continue.");
        sa.assertAll();
    }
}

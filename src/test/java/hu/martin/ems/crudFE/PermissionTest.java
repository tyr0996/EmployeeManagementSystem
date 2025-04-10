package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AccessManagementHeader;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.PermissionPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.PermissionSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PermissionTest extends BaseCrudTest {
    @BeforeMethod
    public void beforeMethod() throws IOException {
        resetRolesAndPermissions();
    }

    @Test
    public void permissionCreateTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoCreateTestData testResult = page.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission saved: ");
    }

    @Test
    public void permissionReadTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoReadTestData testResult = page.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
    }

    @Test
    public void permissionDeleteTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoDeleteTestData testResult = page.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission deleted: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

    @Test
    public void permissionUpdateTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);

        DoUpdateTestData testResult = page.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission updated: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

    @Test
    public void permissionRestoreTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);

        DoRestoreTestData testResult = page.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission restored: ");

        page = new PermissionPage(driver, port);
        page.getGrid().applyFilter(testResult.getResult().getRestoredData());
        page.getGrid().waitForRefresh();
        assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        page.getGrid().resetFilter();
    }

    @Test
    public void permissionPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoPermanentlyDeleteTestData testResult = page.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        page.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);

        DoUpdateFailedTestData testResult = page.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Permission modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);

        DoDeleteFailedTestData testResult = page.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 14);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoCreateFailedTestData testResult = page.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void gettingPermissionsFailed() throws SQLException {
//        Mockito.doReturn(null).when(spyPermissionService).findAll(true); //ApiClientben.findAllWithDeleted();
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 2);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        int countElements = page.getGrid().getPaginationData().getTotalElements();
        assertEquals(countElements, 0);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Error happened while getting permissions");
        notification.close();

        page.getShowDeletedCheckBox().setStatus(true);
        page.getGrid().waitForRefresh();
        assertEquals(page.getGrid().getPaginationData().getTotalElements(), 0);

        VaadinNotificationComponent notification_2 = new VaadinNotificationComponent(driver);
        assertEquals(notification_2.getText(), "Error happened while getting permissions");
        notification_2.close();

        assertEquals(page.getGrid().getPaginationData().getTotalElements(), countElements);
    }


    @Test
    public void findAllPermissionFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 2);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        int countElements = page.getGrid().getPaginationData().getTotalElements();
        assertEquals(countElements, 0);

        VaadinNotificationComponent notificationBeforeShowDeleted = new VaadinNotificationComponent(driver);
        assertEquals(notificationBeforeShowDeleted.getText(), "Error happened while getting permissions");
        notificationBeforeShowDeleted.close();

        page.getShowDeletedCheckBox().setStatus(true);
        page.getGrid().waitForRefresh();
        assertEquals(countElements, 0);

        VaadinNotificationComponent notificationAfterShowDeleted = new VaadinNotificationComponent(driver);
        assertEquals(notificationAfterShowDeleted.getText(), "Error happened while getting permissions");
        notificationAfterShowDeleted.close();

        assertEquals(page.getGrid().getPaginationData().getTotalElements(), countElements);
    }


    @Test
    public void databaseUnavailableWhenSaving() throws SQLException {
//        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 3);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoCreateFailedTestData testResult = page.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void databaseUnavailableWhenGettingRolesForCreateDialog() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();
        PermissionPage page = new PermissionPage(driver, port);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        page.getCreateButton().click();
        PermissionSaveOrUpdateDialog dialog = new PermissionSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        SoftAssert sa = new SoftAssert();
        sa.assertFalse(dialog.getRolesComboBox().isEnabled());
        sa.assertEquals(dialog.getRolesComboBox().getErrorMessage(), "Error happened while getting roles");
        sa.assertFalse(dialog.getSaveButton().isEnabled());
        dialog.close();
        sa.assertAll();
    }

    @AfterClass
    public void afterClass() throws IOException {
        resetRolesAndPermissions();
    }
}

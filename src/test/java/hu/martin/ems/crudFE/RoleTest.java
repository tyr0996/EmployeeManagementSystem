package hu.martin.ems.crudFE;

import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AccessManagementHeader;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.RolePage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.RoleSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoleTest extends BaseCrudTest {
    @BeforeMethod
    public void beforeMethod() throws IOException {
        resetRolesAndPermissions();
    }

    @Test
    public void NotUsedPermissionFilterTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        page.getGrid().applyFilter("", "This permission not used - only for testing");
        page.getGrid().waitForRefresh();
        SoftAssert sa = new SoftAssert();
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 0);

        sa.assertAll();
        page.getGrid().resetFilter();
        page.getGrid().waitForRefresh();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void roleCreateTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoCreateTestData testResult = page.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Role saved: ");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void roleReadTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoReadTestData testResult = page.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void roleDeleteTest() throws IOException {
        resetRolesAndPermissions();
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoDeleteTestData testResult = page.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Role deleted: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void roleUpdateTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);

        DoUpdateTestData testResult = page.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Role updated: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    @Video
    public void roleRestoreTest() throws IOException {
        resetRolesAndPermissions();
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        SoftAssert sa = new SoftAssert();

        DoRestoreTestData testResult = page.doRestoreTest();

        sa.assertEquals((int) testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1, "Deleted row number after method not decreased");
        sa.assertEquals((int) testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1, "Non deleted after method not increased");
        sa.assertTrue(testResult.getNotificationWhenPerform().contains("Role restored: "), "Restore notification not match");

        page = new RolePage(driver, port);
        page.getGrid().applyFilter(testResult.getResult().getRestoredData());
        page.getGrid().waitForRefresh();
        sa.assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0, "Restored element not found in deleted elements (filtered)");
        sa.assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1, "Restored element not found in Non deleted elements (filtered)");
        page.getGrid().resetFilter();
        sa.assertNull(VaadinNotificationComponent.hasNotification(driver), "There were at least one notification after test");

        sa.assertAll();
    }

    @Test
    @Video
    public void rolePermanentlyDeleteTest() throws IOException {
        resetRolesAndPermissions();
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        SoftAssert sa = new SoftAssert();

        RolePage page = new RolePage(driver, port);
        DoPermanentlyDeleteTestData testResult = page.doPermanentlyDeleteTest();
        sa.assertTrue(testResult.getNotificationWhenPerform().contains("Role permanently deleted: "), "performing notification message not contains");


        sa.assertEquals((int) testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1, "Total deleted row number (after perform) not decressed");
        sa.assertEquals((int) testResult.getNonDeletedRowNumberAfterMethod(), (int) testResult.getOriginalNonDeletedRowNumber(), "Total non deleted row number (after perform) changed");
        page.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        sa.assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), "Total deleted row number is not 0 (filtered)");
        sa.assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), "Total non deleted row number is not 0 (filtered)");
        page.getGrid().resetFilter();

        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        sa.assertEquals(notificationComponent.getText(), "Clearing database was successful", "Clearing database notification not match");
        notificationComponent.close();
        sa.assertNull(VaadinNotificationComponent.hasNotification(driver), "There were minimum one notification at the end of the test");
        sa.assertAll();
    }

    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);

        DoUpdateFailedTestData testResult = page.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Role modifying failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);

        DoDeleteFailedTestData testResult = page.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Database error");

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileGettingLoggedInUser() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(2));
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();
        RolePage page = new RolePage(driver, port);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Unable to get the current user. Deleting and editing roles are disabled");
        notification.close();

        int count = page.getGrid().getPaginationData().getTotalElements();
        int pageSize = page.getGrid().getPaginationData().getPageSize();
        SoftAssert sa = new SoftAssert();
        for (int i = 0; i < count && i < pageSize; i++) {
            sa.assertFalse(page.getGrid().getModifyButton(i).isEnabled());
            sa.assertFalse(page.getGrid().getDeleteButton(i).isEnabled());
        }
        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void nullResponseFromServiceWhenCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoCreateFailedTestData testResult = page.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Role saving failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    @Test
    public void nullReturnWhenGettingPermissionsOnCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        page.getCreateButton().click();

        RoleSaveOrUpdateDialog dialog = new RoleSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "EmsError happened while getting permissions");
        dialog.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void nullResponseWhenGettingAllPermissionForFilterHeaderRow() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 4);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);

        assertFalse(page.getGrid().getHeaderFilterInputFields().get(1).isEnabled());
        assertEquals(page.getGrid().getHeaderFilterInputFieldErrorMessage(1), "EmsError happened while getting permissions");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void getAllRoleFailed() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(1, 2));
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        SoftAssert sa = new SoftAssert();
        int countElements = page.getGrid().getPaginationData().getTotalElements();
        sa.assertNotEquals(countElements, 0);
        page.getShowDeletedCheckBox().setStatus(true);
        page.getGrid().waitForRefresh();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "EmsError happened while getting roles");
        notification.close();

        sa.assertEquals((int) page.getGrid().getPaginationData().getTotalElements(), countElements);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseUnavailableWhenModifying() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoUpdateFailedTestData testResult = page.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Role modifying failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    @AfterClass
    public void afterClass() throws IOException {
        resetRolesAndPermissions();
    }

}

package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AccessManagementHeader;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.RoleXPermissionPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import static org.testng.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoleXPermissionCreateTest extends BaseCrudTest {
    @BeforeMethod
    public void beforeMethod() throws IOException {
        resetRolesAndPermissions();
    }


    @Test
    public void createRoleXPermissionTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleXPermissionButton().click();

        RoleXPermissionPage page = new RoleXPermissionPage(driver, port);
        page.getRoleComboBox().fillWithRandom();
        page.getPermissionsComboBox().fillWithRandom();
        page.getSaveButton().click();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Role successfully paired!");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test

    public void databaseNotAvailableWhenCreateRoleXPermissionFailedTest() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 6);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleXPermissionButton().click();

        RoleXPermissionPage page = new RoleXPermissionPage(driver, port);
        page.getRoleComboBox().fillWithRandom();
        page.getPermissionsComboBox().fillWithRandom();
        page.getSaveButton().click();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Role pairing failed!");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileGettingAllPermissions() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 2);
        header.getRoleXPermissionButton().click();

        RoleXPermissionPage page = new RoleXPermissionPage(driver, port);
        assertFalse(page.getPermissionsComboBox().isEnabled());
        assertFalse(page.getSaveButton().isEnabled());
        assertTrue(page.getRoleComboBox().isEnabled());
        assertEquals(page.getPermissionsComboBox().getErrorMessage(), "EmsError happened while getting permissions");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileGettingAllRoles() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 1);
        header.getRoleXPermissionButton().click();

        RoleXPermissionPage page = new RoleXPermissionPage(driver, port);

        SoftAssert sa = new SoftAssert();

        sa.assertFalse(page.getRoleComboBox().isEnabled(), "Role combo box is not disabled");
        sa.assertEquals(page.getRoleComboBox().getErrorMessage(), "EmsError happened while getting roles", "Role combo box's error message doesn't match");

        sa.assertFalse(page.getSaveButton().isEnabled(), "Save button is not disabled");
        sa.assertTrue(page.getPermissionsComboBox().isEnabled(), "Permission combo box disabled");
        sa.assertEquals(page.getPermissionsComboBox().getErrorMessage(), "", "Permission combo box has error message");

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileReturnWhileGettingLoggedInUser() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        header.getRoleXPermissionButton().click();

        SoftAssert sa = new SoftAssert();

        RoleXPermissionPage page = new RoleXPermissionPage(driver, port);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "Can't get logged in User object");
        notification.close();

        sa.assertFalse(page.getRoleComboBox().isEnabled());
        sa.assertFalse(page.getPermissionsComboBox().isEnabled());
        sa.assertFalse(page.getSaveButton().isEnabled());
        sa.assertEquals(page.getRoleComboBox().getErrorMessage(), "");
        sa.assertEquals(page.getPermissionsComboBox().getErrorMessage(), "");

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileGettingAllPermissionsAndRoles() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(3, 4));
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleXPermissionButton().click();

        SoftAssert sa = new SoftAssert();

        RoleXPermissionPage page = new RoleXPermissionPage(driver, port);


        sa.assertFalse(page.getRoleComboBox().isEnabled());
        sa.assertFalse(page.getPermissionsComboBox().isEnabled());
        sa.assertEquals(page.getRoleComboBox().getErrorMessage(), "EmsError happened while getting roles");
        sa.assertEquals(page.getPermissionsComboBox().getErrorMessage(), "EmsError happened while getting permissions");
        sa.assertFalse(page.getSaveButton().isEnabled());

        sa.assertAll();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @AfterClass
    public void afterClass() throws IOException {
        resetRolesAndPermissions();
    }
}

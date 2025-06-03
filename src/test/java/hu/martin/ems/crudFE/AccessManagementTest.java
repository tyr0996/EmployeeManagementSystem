package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.pages.*;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import org.openqa.selenium.TimeoutException;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccessManagementTest extends BaseCrudTest {
    @Test
    public void navigationButtonsTest_RoleButton() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();
        RolePage rp = new RolePage(driver, port);
        AccessManagementHeader newHeader = new AccessManagementHeader(driver, port).initWebElements();

        assertNotNull(newHeader.getRoleButton());
        assertNotNull(newHeader.getPermissionButton());
        assertNotNull(newHeader.getRoleXPermissionButton());
        assertNotNull(rp.getGrid());
        assertNotNull(rp.getCreateButton());
        assertNotNull(rp.getShowDeletedCheckBox());

        newHeader.getRoleButton().click();
        assertThrows(TimeoutException.class, () -> rp.initWebElements());
        newHeader.initWebElements().getRoleButton().click();
        rp.initWebElements();
        assertNotNull(rp.getGrid());
        assertNotNull(rp.getCreateButton());
        assertNotNull(rp.getShowDeletedCheckBox());

    }

    @Test
    public void navigationButtonsTest_PermissionButton() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();
        PermissionPage pp = new PermissionPage(driver, port);
        AccessManagementHeader newHeader = new AccessManagementHeader(driver, port).initWebElements();

        assertNotNull(newHeader.getRoleButton());
        assertNotNull(newHeader.getPermissionButton());
        assertNotNull(newHeader.getRoleXPermissionButton());
        assertNotNull(pp.getGrid());
        assertNotNull(pp.getCreateButton());
        assertNotNull(pp.getShowDeletedCheckBox());

        newHeader.getPermissionButton().click();
        assertThrows(TimeoutException.class, () -> pp.initWebElements());
        newHeader.initWebElements().getRoleButton().click();
        pp.initWebElements();
        assertNotNull(pp.getGrid());
        assertNotNull(pp.getCreateButton());
        assertNotNull(pp.getShowDeletedCheckBox());
    }

    @Test
    public void navigationButtonsTest_RoleXPermiossionButton() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleXPermissionButton().click();
        RoleXPermissionPage rxpp = new RoleXPermissionPage(driver, port);
        AccessManagementHeader newHeader = new AccessManagementHeader(driver, port).initWebElements();

        assertNotNull(newHeader.getRoleButton());
        assertNotNull(newHeader.getPermissionButton());
        assertNotNull(newHeader.getRoleXPermissionButton());
        assertNotNull(rxpp.getPermissionsComboBox());
        assertNotNull(rxpp.getRoleComboBox());
        assertNotNull(rxpp.getSaveButton());

        newHeader.getRoleXPermissionButton().click();
        assertThrows(TimeoutException.class, () -> rxpp.initWebElements());
        newHeader.initWebElements().getRoleXPermissionButton().click();
        rxpp.initWebElements();
        assertNotNull(rxpp.getPermissionsComboBox());
        assertNotNull(rxpp.getRoleComboBox());
        assertNotNull(rxpp.getSaveButton());
    }
}

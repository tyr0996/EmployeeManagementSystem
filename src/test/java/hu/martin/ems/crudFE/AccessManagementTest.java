package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.pages.*;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

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
        assertNotNull(rp.getShowDeletedSwitch());

        newHeader.getRoleButton().click();
        rp.getWait().until(ExpectedConditions.invisibilityOfAllElements(
                rp.getCreateButton().getElement(),
                rp.getGrid().getElement()
        ));
        newHeader.initWebElements().getRoleButton().click();
        rp.initWebElements();
        assertNotNull(rp.getGrid());
        assertNotNull(rp.getCreateButton());
        assertNotNull(rp.getShowDeletedSwitch());

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
        assertNotNull(pp.getShowDeletedSwitch());

        newHeader.getPermissionButton().click();
        pp.getWait().until(ExpectedConditions.invisibilityOfAllElements(
                pp.getCreateButton().getElement(),
                pp.getGrid().getElement()
                ));
        newHeader.initWebElements().getRoleButton().click();
        pp.initWebElements();
        assertNotNull(pp.getGrid());
        assertNotNull(pp.getCreateButton());
        assertNotNull(pp.getShowDeletedSwitch());
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
        rxpp.getRoleComboBox().getWait().until(ExpectedConditions.invisibilityOfAllElements(rxpp.getRoleComboBox().getElement(),
                                                                                            rxpp.getPermissionsComboBox().getElement(),
                                                                                            rxpp.getSaveButton().getElement()
        ));
//        assertThrows(TimeoutException.class, rxpp::initWebElements);
        newHeader.initWebElements().getRoleXPermissionButton().click();
        rxpp.initWebElements();
        assertNotNull(rxpp.getPermissionsComboBox());
        assertNotNull(rxpp.getRoleComboBox());
        assertNotNull(rxpp.getSaveButton());
    }
}

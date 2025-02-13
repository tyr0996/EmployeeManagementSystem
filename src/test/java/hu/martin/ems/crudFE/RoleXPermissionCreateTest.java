package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.model.Role;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RoleXPermissionCreateTest extends BaseCrudTest {

    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;


    //Ezek nem tudom, hogy mire kellenek
    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout[2]/vaadin-checkbox";
    private static final String gridXpath = contentXpath + "/vaadin-grid";
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout[2]/vaadin-button";
    private static final String rolesButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[1]";
    private static final String permissionsButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[2]";
    private static final String roleXPermisisonPairingButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[3]";

    private static final String roleDropboxXpath = contentXpath + "/vaadin-form-layout/vaadin-combo-box";
    private static final String permissionsMultiselectComboBoxXpath = contentXpath + "/vaadin-form-layout/vaadin-multi-select-combo-box";

    private static final String saveButtonXpath = contentXpath + "/vaadin-form-layout/vaadin-button";

    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.ACESS_MANAGEMENT_SUBMENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Permission", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    public void createRoleXPermissionTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        fillElementWith(findVisibleElementWithXpath(roleDropboxXpath), false, "");
        fillElementWith(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath), false, "");
        findClickableElementWithXpathWithWaiting(saveButtonXpath).click();
        checkNotificationContainsTexts("Role successfully paired!");
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseWhenCreateRoleXPermissionFailedTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);

        Mockito.doReturn(null).when(spyRoleService).update(Mockito.any(Role.class));
        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        fillElementWith(findVisibleElementWithXpath(roleDropboxXpath), false, "");
        fillElementWith(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath), false, "");
        findClickableElementWithXpathWithWaiting(saveButtonXpath).click();
        checkNotificationContainsTexts("Role pairing failed!");
        checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void notExpectedStatusCodeWhileGettingPariedPermissionsTo() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedPermissionsTo(any(Role.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
//        Thread.sleep(100);
//        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), true);
//        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "");
//        fillElementWith(findVisibleElementWithXpath(roleDropboxXpath), false, "");
//        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), false);
//        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(saveButtonXpath)), false);
//        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "Error happened while getting paired permissions");
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    public void nullReturnWhileGettingAllPermissions() throws InterruptedException {
        Mockito.doReturn(null).when(spyPermissionService).findAll(false);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), false);
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(saveButtonXpath)), false);
        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "Error happened while getting permissions");
        fillElementWith(findVisibleElementWithXpath(roleDropboxXpath), false, "");
        checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void notExpectedStatusCodeWhileGettingAllPermissions() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionApiClient).findAll();
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
//        Thread.sleep(100);
//        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), false);
//        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(saveButtonXpath)), false);
//        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "Error happened while getting permissions");
//        fillElementWith(findVisibleElementWithXpath(roleDropboxXpath), false, "");
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    public void nullReturnWhileGettingAllRoles() throws InterruptedException {
        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(roleDropboxXpath)), false);
        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(roleDropboxXpath)), "Error happened while getting roles");
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(saveButtonXpath)), false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void databaseNotAvailableWhileReturnWhileGettingLoggedInUser() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        mockDatabaseNotAvailable(getClass(), spyDataSource, 3);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(roleDropboxXpath)), false);
        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(roleDropboxXpath)), "Error happened while getting roles");
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(saveButtonXpath)), false);
        checkNotificationText("Can't get logged in User object");
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseWhileGettingAllPermissionsAndRoles() throws InterruptedException {
        Mockito.doReturn(null).when(spyPermissionService).findAll(false);
        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), false);
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(roleDropboxXpath)), false);
        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "Error happened while getting permissions");
        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(roleDropboxXpath)), "Error happened while getting roles");
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(saveButtonXpath)), false);
        checkNoMoreNotificationsVisible();
    }
}

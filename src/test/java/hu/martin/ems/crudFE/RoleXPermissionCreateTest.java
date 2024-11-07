package hu.martin.ems.crudFE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.vaadin.component.AccessManagement.RoleXPermissionCreate;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;

import static org.testng.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RoleXPermissionCreateTest extends BaseCrudTest {

    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;


    //Ezek nem tudom, hogy mire kellenek
    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";

    private static final String rolesButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button[1]";
    private static final String permissionsButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button[2]";
    private static final String roleXPermisisonPairingButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button[3]";


    private static final String roleDropboxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-form-layout/vaadin-combo-box";
    private static final String permissionsMultiselectComboBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-form-layout/vaadin-multi-select-combo-box";

    private static final String saveButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-form-layout/vaadin-button";

    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.ACESS_MANAGEMENT_SUBMENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Permission", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void createRoleXPermissionTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        fillElementWith(findVisibleElementWithXpath(roleDropboxXpath), false, "");
        fillElementWith(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath), false, "");
        findClickableElementWithXpath(saveButtonXpath).click();
        checkNotificationContainsTexts("Role successfully paired!");
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void createFailedTest() throws JsonProcessingException, InterruptedException {
        Mockito.doThrow(JsonProcessingException.class).when(spyRoleXPermissionApiClient).writeValueAsString(any(RoleXPermission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        fillElementWith(findVisibleElementWithXpath(roleDropboxXpath), false, "");
        fillElementWith(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath), false, "");
        findClickableElementWithXpath(saveButtonXpath).click();
        checkNotificationContainsTexts("Role-permission pairing failed");
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void notExpectedStatusCodeWhileGettingPariedPermissionsTo() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedPermissionsTo(any(Role.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), true);
        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "");
        fillElementWith(findVisibleElementWithXpath(roleDropboxXpath), false, "");
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), false);
        assertEquals(GridTestingUtil.isEnabled(findVisibleElementWithXpath(saveButtonXpath)), false);
        assertEquals(getFieldErrorMessage(findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "Error happened while getting paired permissions");
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void notExpectedStatusCodeWhileGettingAllPermissions() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionApiClient).findAll();
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

    @Test
    public void notExpectedStatusCodeWhileGettingAllRoles() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).findAll();
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
    public void notExpectedStatusCodeWhileGettingAllPermissionsAndRoles() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionApiClient).findAll();
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).findAll();
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

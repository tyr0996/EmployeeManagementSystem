package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Role;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RoleTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";

    private static final String rolesButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button[1]";
    private static final String permissionsButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button[2]";
    private static final String roleXPermisisonPairingButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button[3]";

    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.ACESS_MANAGEMENT_SUBMENU;


    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Role", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void roleCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.createTest();
    }

    @Test
    public void roleReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.readTest();
        Thread.sleep(100);

    }

    @Test
    public void roleDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void roleUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.updateTest();
    }

    @Test
    public void roleRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void rolePermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.permanentlyDeleteTest();
    }

    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }


    @Test
    public void apiSendInvalidStatusCodeWhenSave() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).save(any(Role.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void apiSendInvalidStatusCodeWhenModify() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).update(any(Role.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
        checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void apiSendInvalidStatusCodeWhenSaveRoleXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        checkNoMoreNotificationsVisible();
//    }
//
//    @Test
//    public void apiSendInvalidStatusCodeWhenUpdateRoleSaveRoleXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
//        checkNoMoreNotificationsVisible();
//    }
//
//    @Test
//    public void sendInvalidStatusCodeWhenGettingAllRoleXPermissions() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllWithUnused();
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(100);
//        checkNotificationText("Error happened while getting role-permission pairs");
//        assertEquals(0, countVisibleGridDataRows(gridXpath));
//        assertEquals(0, countHiddenGridDataRows(gridXpath, showDeletedChecBoxXpath));
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    public void sendInvalidStatusCodeWhenGettingPermissionsOnCreate() throws InterruptedException {
        Mockito.doCallRealMethod().doReturn(new EmsResponse(522, "")).when(spyPermissionApiClient).findAll();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(500);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Permission", "Error happened while getting permissions");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
        checkNoMoreNotificationsVisible();
    }
//
//    @Test
//    public void sendInvalidStatusCodeWhenGettingAlRoleXPermissionByRole() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(500);
//        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
//        failedFieldData.put("Permission", "Error happened while getting paired permissions to role");
//
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedPermissionsTo(any(Role.class));
//        crudTestingUtil.updateUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
//        checkNoMoreNotificationsVisible();
//    }

//    @Test
//    public void sendInvalidStatusCodeWhenFindAlRoleXPermissionByRole() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(500);
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
////        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).findAllPairedPermissionsTo(any(Role.class));
//        crudTestingUtil.createTest(null, "Not expected status-code in saving permission to role", false);
//        checkNoMoreNotificationsVisible();
//    }

//    @Test
//    public void sendInvalidStatusCodeWhenGettingAlRoleXPermissionByRoleAfterSaving() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(500);
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).findAlRoleXPermissionByRole(any(Role.class));
//
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        checkNoMoreNotificationsVisible();
//        //checkNotificationText("Error happened while getting role-permission pairs to role");
//    }
//
//    @Test
//    public void sendInvalidStatusCodeWhenRestoreRoleXPermissions() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(500);
//        Mockito.doCallRealMethod().doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).findAlRoleXPermissionByRole(any(Role.class));
//
//        crudTestingUtil.restoreTest("Role restore failed", false);
//        checkNoMoreNotificationsVisible();
//    }
//
//    @Test
//    public void sendInvalidStatusCodeWhenFindPermissionsToRoleWhenTryUpdate() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(1000);
//        Mockito.doCallRealMethod().doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).findAllPairedPermissionsTo(any(Role.class));
//        crudTestingUtil.updateTest(null, "Error happened while getting permissions to role", false);
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    public void sendInvalidStatusCodeWhenGettingAllPermissionForFilterHeaderRow() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyPermissionApiClient).findAll();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(1000);

        assertEquals(GridTestingUtil.isEnabled(TestingUtils.getParent(getHeaderFilterInputFields(gridXpath).get(1))), false);
        assertEquals(GridTestingUtil.getFieldErrorMessage(TestingUtils.getParent(getHeaderFilterInputFields(gridXpath).get(1))), "Error happened while getting permissions");
        checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void sendInvalidStatusCodeWhenDeleteRoleXPermissions() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(500);
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).findAlRoleXPermissionByRole(any(Role.class));
//
//        crudTestingUtil.deleteTest("Role-permission pair deleting failed", false);
//        checkNoMoreNotificationsVisible();
//    }

//    @Test
//    public void failedGettingThePermissionsOfTheSecondRole() throws InterruptedException {
//        Mockito.doCallRealMethod().doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedPermissionsTo(any(Role.class));
//
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(500);
//        checkNotificationText("Error happened while getting role-permission pairs");
//        checkNoMoreNotificationsVisible();
//        assertEquals(countVisibleGridDataRows(gridXpath), 0);
//        assertEquals(countHiddenGridDataRows(gridXpath, showDeletedChecBoxXpath), 0);
////        Mockito.doCallRealMethod().doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
////        crudTestingUtil.createTest(null, "Not expected status-code in saving permission to role", false);
////        checkNoMoreNotificationsVisible();
//    }

    @Test
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(500);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(spyDataSource, null, null, 0);
    }
}

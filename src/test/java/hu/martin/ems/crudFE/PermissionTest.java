package hu.martin.ems.crudFE;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PermissionTest extends BaseCrudTest {
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
        crudTestingUtil = new CrudTestingUtil(driver, "Permission", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void permissionCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.createTest();
    }

    @Test
    public void permissionReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.readTest();
        Thread.sleep(100);

    }

    @Test
    public void permissionDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void permissionUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.updateTest();
    }

    @Test
    public void permissionRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void permissionPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }

    @Test
    public void createFailedTest() throws JsonProcessingException, InterruptedException {
        crudTestingUtil.createFailedTest(port, spyPermissionApiClient, mainMenu, subMenu, permissionsButtonXPath);
    }

    @Test
    public void createFailedTestRoleXPermission() throws JsonProcessingException, InterruptedException {
        crudTestingUtil.createFailedTest(port, spyRoleXPermissionApiClient, mainMenu, subMenu, permissionsButtonXPath);
    }

    @Test
    public void updateFailedTest() throws InterruptedException, JsonProcessingException {
        crudTestingUtil.modifyFailedTest(port, spyPermissionApiClient, mainMenu, subMenu, permissionsButtonXPath);
    }

    @Test
    public void updateFailedTestRoleXPermission() throws JsonProcessingException, InterruptedException {
        crudTestingUtil.modifyFailedTest(port, spyRoleXPermissionApiClient, mainMenu, subMenu, permissionsButtonXPath);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenSaveRoleXPermission() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenUpdateRoleRoleXPermission() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenSavePermission() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyPermissionApiClient).save(any(Permission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenModifyPermission() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyPermissionApiClient).update(any(Permission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
    }

    @Test
    public void undoSaveFailed() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void gettingPermissionsFailed() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionApiClient).findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        checkNotificationText("Error happened while getting permissions");
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void findAllPairedRoleToPermissionsFailed() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedRoleTo(any(Permission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        LinkedHashMap<String, String> failed = new LinkedHashMap<>();
        failed.put("Roles", "Error happened while getting paired roles");
        crudTestingUtil.updateUnexpectedResponseCodeWhileGettingData(null, failed);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void findAllRoleToPermissionsFailed() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).findAll();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        LinkedHashMap<String, String> failed = new LinkedHashMap<>();
        failed.put("Roles", "Error happened while getting roles");
        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failed);
        checkNoMoreNotificationsVisible();
    }

    @Test
    public void findAllPariedRoleToWhenUpdate() throws InterruptedException {

        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        LinkedHashMap<String, String> failed = new LinkedHashMap<>();
        failed.put("Roles", "Error happened while getting roles");
        Mockito.doCallRealMethod().doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedRoleTo(any(Permission.class));
        crudTestingUtil.updateTest(null, "Error happened while getting paired permissions", false);
        checkNoMoreNotificationsVisible();
    }

    //létrehozás sikertelen, visszaállítás sikertelen roleXPermissionApi.findAllPairedRoleTo(permission);
    @Test
    public void createFailedUndoSaveFailed() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedRoleTo(any(Permission.class));
        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
        checkNoMoreNotificationsVisible();
    }
}

package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.*;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
public class PermissionTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout[2]/vaadin-checkbox";
    private static final String gridXpath = contentXpath + "/vaadin-grid";
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout[2]/vaadin-button";
    private static final String rolesButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[1]";
    private static final String permissionsButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[2]";
    private static final String roleXPermisisonPairingButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[3]";

    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.ACESS_MANAGEMENT_SUBMENU;


    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Permission", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @BeforeMethod
    public void beforeMethod(){
        resetRolesAndPermissions();
    }

    @Test
    @Video
    public void permissionCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.createTest();
    }

    @Test
    @Video
    public void permissionReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.readTest();
        Thread.sleep(100);

    }

    @Test
    @Video
    public void permissionDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.deleteTest();
    }

    @Test
    @Video
    public void permissionUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.updateTest();
    }

    @Test
    @Video
    public void permissionRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.restoreTest();
    }

    @Test
    @Video
    public void permissionPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.permanentlyDeleteTest();
    }

    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }

//    @Test
//    public void apiSendInvalidStatusCodeWhenSaveRoleXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//    }
//
//    @Test
//    public void apiSendInvalidStatusCodeWhenUpdateRoleRoleXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
//    }


    @Test
    @Video
    public void databaseNotAvailableWhenModify() throws InterruptedException, SQLException {
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyPermissionApiClient).update(any(Permission.class));
//        Mockito.doReturn(null).when(spyPermissionService).update(any(Permission.class));
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 6);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.updateTest(null, "Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

    @Test
    @Video
    public void databaseNotAvailableWhenCreate() throws InterruptedException, SQLException {
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 10);
//        Mockito.doReturn(null).when(spyPermissionService).save(any(Permission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        crudTestingUtil.createTest(null, "Permission saving failed: Internal server error", false);
        checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void undoSaveFailed() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void gettingPermissionsFailed() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyPermissionService).findAll(true); //ApiClientben.findAllWithDeleted();
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 2);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        checkNotificationText("Error happened while getting permissions");
        checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void findAllPairedRoleToPermissionsFailed() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedRoleTo(any(Permission.class));
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        Thread.sleep(100);
//        LinkedHashMap<String, String> failed = new LinkedHashMap<>();
//        failed.put("Roles", "Error happened while getting paired roles");
//        crudTestingUtil.updateUnexpectedResponseCodeWhileGettingData(null, failed);
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void findAllRoleFailed() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 3);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        LinkedHashMap<String, String> failed = new LinkedHashMap<>();
        failed.put("Roles", "Error happened while getting roles");
        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failed);
//        checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void findAllPariedRoleToWhenUpdate() throws InterruptedException {
//
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        Thread.sleep(100);
//        LinkedHashMap<String, String> failed = new LinkedHashMap<>();
//        failed.put("Roles", "Error happened while getting roles");
//        Mockito.doCallRealMethod().doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedRoleTo(any(Permission.class));
//        crudTestingUtil.updateTest(null, "Error happened while getting paired permissions", false);
//        checkNoMoreNotificationsVisible();
//    }

    //létrehozás sikertelen, visszaállítás sikertelen roleXPermissionApi.findAllPairedRoleTo(permission);
//    @Test
//    public void createFailedUndoSaveFailed() throws InterruptedException {
//        TestingUtils.loginWith(driver, port, "admin", "admin");
//        navigateMenu(mainMenu, subMenu);
//        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        Thread.sleep(100);
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedRoleTo(any(Permission.class));
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }

    @AfterClass
    public void afterClass(){
        resetRolesAndPermissions();
    }
}

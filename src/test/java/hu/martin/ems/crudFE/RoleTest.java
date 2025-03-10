package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.JPAConfig;
import hu.martin.ems.core.config.LogNumberingConverter;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.*;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
public class RoleTest extends BaseCrudTest {
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


    private GridTestingUtil gridTestingUtil;

    

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(getDriver());
        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, getDriver(), "Role", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(getDriver(), Duration.ofMillis(5000));
    }

    @BeforeMethod
    public void beforeMethod(){
        resetRolesAndPermissions();
    }

    @Test
    @Video
    public void roleCreateTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.createTest();
    }

    @Test
    @Video
    public void roleReadTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.readTest();
        Thread.sleep(100);

    }

    @Test
    @Video
    public void roleDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.deleteTest();
    }

    @Test
    @Video
    public void roleUpdateTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.updateTest();
    }

    @Test
    @Video
    public void roleRestoreTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.restoreTest();
    }

    @Test
    @Video
    public void rolePermanentlyDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.permanentlyDeleteTest();
    }

    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }


//    @Test(enabled = false)
//    public void apiSendInvalidStatusCodeWhenSave() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).save(any(Role.class));
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }
//
//    @Test(enabled = false)
//    public void apiSendInvalidStatusCodeWhenModify() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).update(any(Role.class));
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void databaseNotAvailableWhenModify() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyRoleService).update(any(Role.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 8);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.updateTest(null, "Role modifying failed: Internal server error", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

    @Test
    @Video
    public void databaseNotAvailableWhileGettingLoggedInUser() throws InterruptedException, SQLException {
        LogNumberingConverter.resetCounter();
        JPAConfig.resetCallIndex();
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(this, spyDataSource, 5);

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

//        mockDatabaseNotAvailableWhen(this.getClass(), spyDataSource, () -> spyUserService.findByUsername("admin"), () -> spyOrderService.findAll(false));
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(1000);
        gridTestingUtil.checkNotificationText("Unable to get the current user. Deleting and editing roles are disabled");
    }

    @Test
    @Video
    public void nullResponseFromServiceWhenCreate() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 14);
//        Mockito.doReturn(null).when(spyRoleService).save(any(Role.class));
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.createTest(null, "Role saving failed: Internal server error", false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void apiSendInvalidStatusCodeWhenSaveRoleXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }
//
//    @Test
//    public void apiSendInvalidStatusCodeWhenUpdateRoleSaveRoleXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }
//
//    @Test
//    public void sendInvalidStatusCodeWhenGettingAllRoleXPermissions() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllWithUnused();
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(100);
//        gridTestingUtil.checkNotificationText("Error happened while getting role-permission pairs");
//        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(gridXpath));
//        assertEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void nullReturnWhenGettingPermissionsOnCreate() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 6);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(500);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Permission", "Error happened while getting permissions");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void nullResponseWhenGettingAllPermissionForFilterHeaderRow() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(1000);

        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.getParent(gridTestingUtil.getHeaderFilterInputFields(gridXpath).get(1))), false);
        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.getParent(gridTestingUtil.getHeaderFilterInputFields(gridXpath).get(1))), "Error happened while getting permissions");
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void getAllRoleFailed() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 6);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

//        Mockito.doReturn(null).when(spyRoleService).findAllWithGraph(true); //ApiClient-ben findAllWithGraphWithDeleted()
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        gridTestingUtil.checkNoMoreNotificationsVisible();
        int roleNumber = gridTestingUtil.countVisibleGridDataRows(gridXpath);
        assertNotEquals(roleNumber, 0);
        Thread.sleep(100);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(showDeletedCheckBoxXpath).click();
        Thread.sleep(100);

        gridTestingUtil.checkNotificationText("Error happened while getting roles");
        assertEquals(gridTestingUtil.countVisibleGridDataRows(gridXpath), roleNumber);

        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseUnavailableWhenModifying() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(500);
        crudTestingUtil.databaseUnavailableWhenUpdateEntity(spyDataSource, null, null, 0);
    }

    @Test
    @Video
    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(500);
         crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
    }

    @AfterClass
    public void afterClass(){
        resetRolesAndPermissions();
    }

}

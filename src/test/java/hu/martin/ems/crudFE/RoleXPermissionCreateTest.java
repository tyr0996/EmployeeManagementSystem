package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.*;

import java.sql.SQLException;
import java.time.Duration;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
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

    private GridTestingUtil gridTestingUtil;

    

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(getDriver());
        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, getDriver(), "Permission", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(getDriver(), Duration.ofMillis(5000));
    }

    @BeforeMethod
    public void beforeMethod(){
        resetRolesAndPermissions();
    }

    public void createRoleXPermissionTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        gridTestingUtil.fillElementWith(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath), false, "");
        gridTestingUtil.fillElementWith(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath), false, "");
        gridTestingUtil.findClickableElementWithXpathWithWaiting(saveButtonXpath).click();
        gridTestingUtil.checkNotificationContainsTexts("Role successfully paired!");
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseNotAvailableWhenCreateRoleXPermissionFailedTest() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 6);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);

//        Mockito.doReturn(null).when(spyRoleService).update(Mockito.any(Role.class));
        gridTestingUtil.findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        gridTestingUtil.fillElementWith(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath), false, "");
        gridTestingUtil.fillElementWith(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath), false, "");
        gridTestingUtil.findClickableElementWithXpathWithWaiting(saveButtonXpath).click();
        gridTestingUtil.checkNotificationContainsTexts("Role pairing failed!");
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void notExpectedStatusCodeWhileGettingPariedPermissionsTo() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllPairedPermissionsTo(any(Role.class));
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
//        Thread.sleep(100);
//        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), true);
//        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "");
//        gridTestingUtil.fillElementWith(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath), false, "");
//        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), false);
//        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(saveButtonXpath)), false);
//        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "Error happened while getting paired permissions");
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void databaseNotAvailableWhileGettingAllPermissions() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyPermissionService).findAll(false);
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), false);
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(saveButtonXpath)), false);
        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "Error happened while getting permissions");
        gridTestingUtil.fillElementWith(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath), false, "");
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void notExpectedStatusCodeWhileGettingAllPermissions() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionApiClient).findAll();
//        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
//        Thread.sleep(100);
//        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), false);
//        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(saveButtonXpath)), false);
//        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "Error happened while getting permissions");
//        gridTestingUtil.fillElementWith(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath), false, "");
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    @Video
    public void databaseNotAvailableWhileGettingAllRoles() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 2);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath)), false);
        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath)), "Error happened while getting roles");
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(saveButtonXpath)), false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseNotAvailableWhileReturnWhileGettingLoggedInUser() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath)), false);
        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath)), "Error happened while getting roles");
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(saveButtonXpath)), false);
        gridTestingUtil.checkNotificationText("Can't get logged in User object");
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseNotAvailableWhileGettingAllPermissionsAndRoles() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyPermissionService).findAll(false);
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        gridTestingUtil.mockDatabaseNotAvailableWhen(getClass(), spyDataSource, Arrays.asList(2, 3));

        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.findClickableElementWithXpathWithWaiting(roleXPermisisonPairingButtonXPath).click();
        Thread.sleep(100);
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), false);
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath)), false);
        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(permissionsMultiselectComboBoxXpath)), "Error happened while getting permissions");
        assertEquals(gridTestingUtil.getFieldErrorMessage(gridTestingUtil.findVisibleElementWithXpath(roleDropboxXpath)), "Error happened while getting roles");
        assertEquals(gridTestingUtil.isEnabled(gridTestingUtil.findVisibleElementWithXpath(saveButtonXpath)), false);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @AfterClass
    public void afterClass(){
        resetRolesAndPermissions();
    }
}

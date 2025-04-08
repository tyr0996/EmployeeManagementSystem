package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AccessManagementHeader;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.PermissionPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PermissionTest extends BaseCrudTest {
//    private static CrudTestingUtil crudTestingUtil;
//    private static WebDriverWait notificationDisappearWait;
//
//    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout[2]/vaadin-checkbox";
//    private static final String gridXpath = contentXpath + "/vaadin-grid";
//    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout[2]/vaadin-button";
//    private static final String permissionsButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[1]";
//    private static final String permissionsButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[2]";
//    private static final String permissionXPermisisonPairingButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[3]";
//
//    private static final String mainMenu = UIXpaths.ADMIN_MENU;
//    private static final String subMenu = UIXpaths.ACESS_MANAGEMENT_SUBMENU;
//
//
//    private GridTestingUtil gridTestingUtil;
//
//
//
//    @BeforeClass
//    public void setup() {
//        gridTestingUtil = new GridTestingUtil(driver);
//        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "Permission", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
//        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
//    }

    @BeforeMethod
    public void beforeMethod(){
        resetRolesAndPermissions();
    }

    @Test
    public void permissionCreateTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoCreateTestData testResult = page.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission saved: ");
    }

    @Test
    public void permissionReadTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoReadTestData testResult = page.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
    }

    @Test
    public void permissionDeleteTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoDeleteTestData testResult = page.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission deleted: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

    @Test
    public void permissionUpdateTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);

        DoUpdateTestData testResult = page.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission updated: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

    @Test
    public void permissionRestoreTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);

        DoRestoreTestData testResult = page.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission restored: ");

        page = new PermissionPage(driver, port);
        page.getGrid().applyFilter(testResult.getResult().getRestoredData());
        page.getGrid().waitForRefresh();
        assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        page.getGrid().resetFilter();
    }

    @Test
    public void permissionPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoPermanentlyDeleteTestData testResult = page.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        page.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

    //@Test
//    public void extraFilterInvalidValue() throws InterruptedException {
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        NotificationCheck nc = new NotificationCheck();
//        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
//        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
//    }

//    @Test
//    public void apiSendInvalidStatusCodeWhenSavePermissionXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionXPermissionApiClient).save(any(PermissionXPermission.class));
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//    }
//
//    @Test
//    public void apiSendInvalidStatusCodeWhenUpdatePermissionPermissionXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyPermissionXPermissionApiClient).save(any(PermissionXPermission.class));
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
//    }


    @Test
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);

        DoUpdateFailedTestData testResult = page.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Permission modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);

        DoDeleteFailedTestData testResult = page.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhenCreate() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 14);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoCreateFailedTestData testResult = page.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

//    @Test
//    public void undoSaveFailed() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionXPermissionApiClient).save(any(PermissionXPermission.class));
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    public void gettingPermissionsFailed() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyPermissionService).findAll(true); //ApiClientben.findAllWithDeleted();
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 2);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        int countElements = page.getGrid().getPaginationData().getTotalElements();
        assertEquals(countElements, 0);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Error happened while getting permissions");
        notification.close();

        page.getShowDeletedCheckBox().setStatus(true);
        page.getGrid().waitForRefresh();
        assertEquals(page.getGrid().getPaginationData().getTotalElements(), 0);

        VaadinNotificationComponent notification_2 = new VaadinNotificationComponent(driver);
        assertEquals(notification_2.getText(), "Error happened while getting permissions");
        notification_2.close();

        assertEquals(page.getGrid().getPaginationData().getTotalElements(), countElements);
    }

//    @Test
//    public void findAllPairedPermissionToPermissionsFailed() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionXPermissionApiClient).findAllPairedPermissionTo(any(Permission.class));
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        Thread.sleep(100);
//        LinkedHashMap<String, String> failed = new LinkedHashMap<>();
//        failed.put("Permissions", "Error happened while getting paired permissions");
//        crudTestingUtil.updateUnexpectedResponseCodeWhileGettingData(null, failed);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    
    public void findAllPermissionFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 2);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        int countElements = page.getGrid().getPaginationData().getTotalElements();
        assertEquals(countElements, 0);

        VaadinNotificationComponent notificationBeforeShowDeleted = new VaadinNotificationComponent(driver);
        assertEquals(notificationBeforeShowDeleted.getText(), "Error happened while getting permissions");
        notificationBeforeShowDeleted.close();

        page.getShowDeletedCheckBox().setStatus(true);
        page.getGrid().waitForRefresh();
        assertEquals(countElements, 0);

        VaadinNotificationComponent notificationAfterShowDeleted = new VaadinNotificationComponent(driver);
        assertEquals(notificationAfterShowDeleted.getText(), "Error happened while getting permissions");
        notificationAfterShowDeleted.close();

        assertEquals(page.getGrid().getPaginationData().getTotalElements(), countElements);
    }

//    @Test
//    public void findAllPariedPermissionToWhenUpdate() throws InterruptedException {
//
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        Thread.sleep(100);
//        LinkedHashMap<String, String> failed = new LinkedHashMap<>();
//        failed.put("Permissions", "Error happened while getting permissions");
//        Mockito.doCallRealMethod().doReturn(new EmsResponse(522, "")).when(spyPermissionXPermissionApiClient).findAllPairedPermissionTo(any(Permission.class));
//        crudTestingUtil.updateTest(null, "Error happened while getting paired permissions", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    //létrehozás sikertelen, visszaállítás sikertelen permissionXPermissionApi.findAllPairedPermissionTo(permission);
//    @Test
//    public void createFailedUndoSaveFailed() throws InterruptedException {
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(permissionsButtonXPath).click();
//        Thread.sleep(100);
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionXPermissionApiClient).save(any(PermissionXPermission.class));
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyPermissionXPermissionApiClient).findAllPairedPermissionTo(any(Permission.class));
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    public void databaseUnavailableWhenSaving() throws SQLException {
//        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 3);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getPermissionButton().click();

        PermissionPage page = new PermissionPage(driver, port);
        DoCreateFailedTestData testResult = page.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Permission saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
    }

    @AfterClass
    public void afterClass(){
        resetRolesAndPermissions();
    }
}

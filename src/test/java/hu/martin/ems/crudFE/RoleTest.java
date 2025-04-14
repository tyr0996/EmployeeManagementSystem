package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AccessManagementHeader;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.RolePage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.RoleSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Listeners(UniversalVideoListener.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RoleTest extends BaseCrudTest {
//    private static CrudTestingUtil crudTestingUtil;
//    private static WebDriverWait notificationDisappearWait;
//
//    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout[2]/vaadin-checkbox";
//    private static final String gridXpath = contentXpath + "/vaadin-grid";
//    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout[2]/vaadin-button";
//    private static final String rolesButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[1]";
//    private static final String permissionsButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[2]";
//    private static final String roleXPermisisonPairingButtonXPath = contentXpath + "/vaadin-horizontal-layout[1]/vaadin-button[3]";
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
//        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "Role", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
//        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
//    }

    @BeforeMethod
    public void beforeMethod() throws IOException {
        resetRolesAndPermissions();
    }

    @Test
    public void roleCreateTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoCreateTestData testResult = page.doCreateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Role saved: ");
    }

    @Test
    public void roleReadTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoReadTestData testResult = page.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
    }

    @Test
    public void roleDeleteTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoDeleteTestData testResult = page.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Role deleted: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

    @Test
    public void roleUpdateTest() throws InterruptedException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);

        DoUpdateTestData testResult = page.doUpdateTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Role updated: ");

        page.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();
    }

    @Test
    public void roleRestoreTest() throws InterruptedException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);

        DoRestoreTestData testResult = page.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("Role restored: ");

        page = new RolePage(driver, port);
        page.getGrid().applyFilter(testResult.getResult().getRestoredData());
        page.getGrid().waitForRefresh();
        assertEquals(page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()), 0);
        assertEquals(page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()), 1);
        page.getGrid().resetFilter();
    }

    @Test
    public void rolePermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoPermanentlyDeleteTestData testResult = page.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("Role permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        page.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, page.getGrid().getTotalDeletedRowNumber(page.getShowDeletedCheckBox()));
        assertEquals(0, page.getGrid().getTotalNonDeletedRowNumber(page.getShowDeletedCheckBox()));
        page.getGrid().resetFilter();

        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
    }

    //@Test
//    public void extraFilterInvalidValue() throws InterruptedException {
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        NotificationCheck nc = new NotificationCheck();
//        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
//        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
//    }


//    @Test(enabled = false)
//    public void apiSendInvalidStatusCodeWhenSave() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).save(any(Role.class));
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }
//
//    @Test(enabled = false)
//    public void apiSendInvalidStatusCodeWhenModify() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).update(any(Role.class));
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    
    public void databaseNotAvailableWhenModify() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);

        DoUpdateFailedTestData testResult = page.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Role modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());

    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);

        DoDeleteFailedTestData testResult = page.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Internal Server Error");


//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
    }

    @Test
    public void databaseNotAvailableWhileGettingLoggedInUser() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 5);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();
        RolePage page = new RolePage(driver, port);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Unable to get the current user. Deleting and editing roles are disabled");
        notification.close();

        int count = page.getGrid().getPaginationData().getTotalElements();
        int pageSize = page.getGrid().getPaginationData().getPageSize();
        for(int i = 0; i < count && i < pageSize; i++){
            assertFalse(page.getGrid().getModifyButton(i).isEnabled());
            assertFalse(page.getGrid().getDeleteButton(i).isEnabled());
        }
    }

    @Test
    
    public void nullResponseFromServiceWhenCreate() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 14);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoCreateFailedTestData testResult = page.doDatabaseNotAvailableWhenCreateTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Role saving failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());


//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 14);
////        Mockito.doReturn(null).when(spyRoleService).save(any(Role.class));
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.createTest(null, "Role saving failed: Internal server error", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

//    @Test
//    public void apiSendInvalidStatusCodeWhenSaveRoleXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }
//
//    @Test
//    public void apiSendInvalidStatusCodeWhenUpdateRoleSaveRoleXPermission() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).doCallRealMethod().when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }
//
//    @Test
//    public void sendInvalidStatusCodeWhenGettingAllRoleXPermissions() throws InterruptedException {
//        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).findAllWithUnused();
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(100);
//        gridTestingUtil.checkNotificationText("Error happened while getting role-permission pairs");
//        assertEquals(0, gridTestingUtil.countVisibleGridDataRows(gridXpath));
//        assertEquals(0, gridTestingUtil.countHiddenGridDataRows(gridXpath, showDeletedCheckBoxXpath));
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//    }

    @Test
    public void nullReturnWhenGettingPermissionsOnCreate() throws SQLException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        page.getCreateButton().click();

        RoleSaveOrUpdateDialog dialog = new RoleSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "Error happened while getting permissions");
        dialog.close();

//        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
//        failedFieldData.put("Permission", "Error happened while getting permissions");
//
//        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    public void nullResponseWhenGettingAllPermissionForFilterHeaderRow() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 3);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);

        assertFalse(page.getGrid().getHeaderFilterInputFields().get(1).isEnabled());
        assertEquals(page.getGrid().getHeaderFilterInputFieldErrorMessage(1), "Error happened while getting permissions");
    }

    @Test
    public void getAllRoleFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableAfter(spyDataSource, 6);
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        int countElements = page.getGrid().getPaginationData().getTotalElements();
        assertNotEquals(countElements, 0);
        page.getShowDeletedCheckBox().setStatus(true);
        page.getGrid().waitForRefresh();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Error happened while getting roles");
        notification.close();

        assertEquals(page.getGrid().getPaginationData().getTotalElements(), countElements);



//        gridTestingUtil.mockDatabaseNotAvailableAfter(getClass(), spyDataSource, 6);
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);

//        Mockito.doReturn(null).when(spyRoleService).findAllWithGraph(true); //ApiClient-ben findAllWithGraphWithDeleted()
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(100);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//        int roleNumber = gridTestingUtil.countVisibleGridDataRows(gridXpath);
//        assertNotEquals(roleNumber, 0);
//        Thread.sleep(100);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(showDeletedCheckBoxXpath).click();
//        Thread.sleep(100);
//
//        gridTestingUtil.checkNotificationText("Error happened while getting roles");
//        assertEquals(gridTestingUtil.countVisibleGridDataRows(gridXpath), roleNumber);
//
//        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    public void databaseUnavailableWhenModifying() throws SQLException, InterruptedException {
        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
        header.getRoleButton().click();

        RolePage page = new RolePage(driver, port);
        DoUpdateFailedTestData testResult = page.doDatabaseNotAvailableWhenUpdateTest(null, null, spyDataSource, 0);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("Role modifying failed: Internal Server Error");
        assertEquals(0, testResult.getResult().getFailedFields().size());



//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(500);
//        crudTestingUtil.databaseUnavailableWhenUpdateEntity(spyDataSource, null, null, 0);
    }

//    @Test
//    public void databaseUnavailableWhenSaving() throws SQLException, InterruptedException {
//        EmptyLoggedInVaadinPage loggedIn = (EmptyLoggedInVaadinPage) (LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true));
//        loggedIn.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ACESS_MANAGEMENT_SUBMENU);
//        AccessManagementHeader header = new AccessManagementHeader(driver, port).initWebElements();
//        header.getRoleButton().click();
//
//        RolePage page = new RolePage(driver, port);
//
//
//
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        gridTestingUtil.findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
//        Thread.sleep(500);
//         crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 0);
//    }

    @AfterClass
    public void afterClass() throws IOException {
        resetRolesAndPermissions();
    }

}

package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.ElementLocation;
import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.AdminToolsPage;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.UserPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.SideMenu;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.UserSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserCrudTest extends BaseCrudTest {

    @BeforeMethod
    public void beforeMethod(){
        resetUsers();
    }

    @Test
    public void userCreateTest() {
        String pass = RandomGenerator.generateRandomOnlyLetterString();
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Password", pass);
        withData.put("Password again", pass);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoCreateTestData testResult = userPage.doCreateTest(withData);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("User saved: ");
        
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void userReadTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoReadTestData testResult = userPage.doReadTest(null, true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());
        
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void userDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoDeleteTestData testResult = userPage.doDeleteTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() + 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() - 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("User deleted: ");

        userPage.getGrid().applyFilter(testResult.getResult().getOriginalDeletedData());
        assertEquals(1, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedCheckBox()));
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()));
        userPage.getGrid().resetFilter();
        
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoDeleteFailedTestData testResult = userPage.doDatabaseNotAvailableWhenDeleteTest(spyDataSource);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("Database error");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void userUpdateTest() {
        String pass = RandomGenerator.generateRandomOnlyLetterString();
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Password", pass);
        withData.put("Password again", pass);

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoUpdateTestData testResult = userPage.doUpdateTest(withData);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("User updated: ");

        userPage.getGrid().applyFilter(testResult.getResult().getOriginalModifiedData());
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedCheckBox()));
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()));
        userPage.getGrid().resetFilter();
        
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenGettingLoggedInUser() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(0)); //2, 3 kizárva. 1- role filter
//        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        SoftAssert sa = new SoftAssert();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "EmsError happened while getting the logged in user. Deletion and modification is disabled", "1-es");
        notification.close();

        UserPage userPage = new UserPage(driver, port);
        sa.assertNotEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()), 0, "2-es");
        for(int i = 0; i < userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()); i++){
            sa.assertFalse(userPage.getGrid().getModifyButton(i).isEnabled(), "4-es");
            sa.assertFalse(userPage.getGrid().getDeleteButton(i).isEnabled(), "5-ös");
        }
//        gridTestingUtil.checkNotificationText("EmsError happened while getting the logged in user. Deletion and modification is disabled");
        sa.assertNull(VaadinNotificationComponent.hasNotification(driver), "6-os");
        sa.assertAll();

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenGettingAllUser() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(2, 3));
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Getting users failed");
        notification.close();

        UserPage userPage = new UserPage(driver, port);
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()), 0);
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedCheckBox()), 0);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void userRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoRestoreTestData testResult = userPage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("User restored: ");

        userPage = new UserPage(driver, port);
        userPage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        userPage.getGrid().waitForRefresh();
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedCheckBox()), 0);
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()), 1);
        userPage.getGrid().resetFilter();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void userPermanentlyDeleteTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoPermanentlyDeleteTestData testResult = userPage.doPermanentlyDeleteTest();
        assertThat(testResult.getNotificationWhenPerform()).contains("User permanently deleted: ");


        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        userPage.getGrid().applyFilter(testResult.getResult().getPermanentlyDeletedData());
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedCheckBox()));
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()));
        userPage.getGrid().resetFilter();

        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.ADMINTOOLS_SUB_MENU);

        AdminToolsPage adminToolsPage = new AdminToolsPage(driver, port);

        adminToolsPage.getClearDatabaseButton().click();
        VaadinNotificationComponent notificationComponent = new VaadinNotificationComponent(driver);
        assertEquals(notificationComponent.getText(), "Clearing database was successful");
        notificationComponent.close();
        
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void createUserAllreadyExists() {
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Username", "admin");

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoCreateTestData testResult = userPage.doCreateTest(withData);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertEquals(testResult.getNotificationWhenPerform(), "Username already exists!");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void modifyUserAllreadyExists() {
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Username", "robi");

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        userPage.getGrid().applyFilter("Erzsi", "$2a$12$XGHOnxr5AyfmOoIjKEEP7.JXIXZgNiB53uf2AhbpwdAFztqi8FqCy", "false");
        DoUpdateTestData testData = userPage.doUpdateTest(withData);
        userPage.getGrid().resetFilter();
        assertEquals(testData.getDeletedRowNumberAfterMethod(), testData.getOriginalDeletedRowNumber());
        assertEquals(testData.getNonDeletedRowNumberAfterMethod(), testData.getOriginalNonDeletedRowNumber());
        assertEquals(testData.getNotificationWhenPerform(), "Username already exists!");
        userPage.getGrid().applyFilter("Erzsi", "$2a$12$XGHOnxr5AyfmOoIjKEEP7.JXIXZgNiB53uf2AhbpwdAFztqi8FqCy", "false");
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()), 1);
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedCheckBox()), 0);
        userPage.getGrid().resetFilter();

//        int users = gridTestingUtil.countVisibleGridDataRows(gridXpath);
//        crudTestingUtil.createTest();

//        gridTestingUtil.applyFilter(gridXpath, "Erzsi", "$2a$12$XGHOnxr5AyfmOoIjKEEP7.JXIXZgNiB53uf2AhbpwdAFztqi8FqCy", "false");
//        crudTestingUtil.updateTest(withData, "Username already exists!", false);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    @Test
    public void createUserPasswordDoesntMatch() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");

        UserPage userPage = new UserPage(driver, port);
        DoUpdateTestData testData = userPage.doUpdateTest(withData);
        assertEquals(testData.getNotificationWhenPerform(), "Passwords doesn't match!");
        assertEquals(testData.getDeletedRowNumberAfterMethod(), testData.getOriginalDeletedRowNumber());
        assertEquals(testData.getNonDeletedRowNumberAfterMethod(), testData.getOriginalNonDeletedRowNumber());


//        crudTestingUtil.createTest(withData, "Passwords doesn't match!", false);
        
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void updateUserEmptyPassword() {
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Password", "");
        withData.put("Password again", "");

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoUpdateTestData testData = userPage.doUpdateTest(withData);
        assertEquals(testData.getDeletedRowNumberAfterMethod(), testData.getOriginalDeletedRowNumber());
        assertEquals(testData.getNonDeletedRowNumberAfterMethod(), testData.getOriginalNonDeletedRowNumber());
        assertEquals(testData.getNotificationWhenPerform(), "Password is required!");
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
//        withData.put("Password", "");
//        withData.put("Password again", "");

//        crudTestingUtil.updateTest(withData, "Password is required!", false);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void updateUserPasswordDoesntMatch() {
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Password", "asd");
        withData.put("Password again", "asdf");

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoUpdateTestData testData = userPage.doUpdateTest(withData);
        assertEquals(testData.getDeletedRowNumberAfterMethod(), testData.getOriginalDeletedRowNumber());
        assertEquals(testData.getNonDeletedRowNumberAfterMethod(), testData.getOriginalNonDeletedRowNumber());
        assertEquals(testData.getNotificationWhenPerform(), "Passwords doesn't match!");
        
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }


    //@Test
//    public void extraFilterInvalidValue() throws InterruptedException {
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        NotificationCheck nc = new NotificationCheck();
//        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
//        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
//
//    }


    @Test
    public void gettingUsersFailed() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 4);
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "Getting users failed");
        notification.close();
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()), 0);
//        gridTestingUtil.checkNotificationText("Getting users failed");
//        gridTestingUtil.checkNoMoreNotificationsVisible();
//        assertEquals(gridTestingUtil.countVisibleGridDataRows(gridXpath), 0);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void updateUserButUsernameNotChanged() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);

        String pass = RandomGenerator.generateRandomOnlyLetterString();
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Username", "robi");
        withData.put("Password", pass);
        withData.put("Password again", pass);

        userPage.getGrid().applyFilter("robi", "", "", "", "");
        String[] original = userPage.getGrid().getDataFromRowLocation(new ElementLocation(1, 0), true);
        DoUpdateTestData testData = userPage.doUpdateTest(withData);

        userPage.getGrid().resetFilter();
        userPage.getGrid().applyFilter("robi", "", "", "", "");
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()), 1);
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedCheckBox()), 0);
        userPage.getGrid().resetFilter();

        userPage.getGrid().applyFilter(original);
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedCheckBox()), 0);
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedCheckBox()), 0);
        userPage.getGrid().resetFilter();


//        gridTestingUtil.applyFilter(gridXpath, "robi", "", "", "");
//        crudTestingUtil.updateTest(withData, null, true);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
        
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenFindAllRole() throws SQLException {
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 5);

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        userPage.getCreateButton().click();

        UserSaveOrUpdateDialog dialog = new UserSaveOrUpdateDialog(driver);
        dialog.initWebElements();
        List<FailedVaadinFillableComponent> failedComponents = dialog.getFailedComponents();
        assertEquals(failedComponents.size(), 1);
        assertEquals(failedComponents.get(0).getErrorMessage(), "EmsError happened while getting roles");
        dialog.close();

//        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
//        gridTestingUtil.loginWith(driver, port, "admin", "29b{}'f<0V>Z");
//        gridTestingUtil.navigateMenu(mainMenu, subMenu);
//        Thread.sleep(500);
//        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
//        failedFieldData.put("Role", "EmsError happened while getting roles");
//
//        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
//        gridTestingUtil.checkNoMoreNotificationsVisible();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenFindAllRoleInFilter() throws SQLException {
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);


        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 1);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);

        SoftAssert sa = new SoftAssert();

        sa.assertFalse(userPage.getGrid().getHeaderFilterInputFields().get(3).isEnabled());
        sa.assertEquals(userPage.getGrid().getHeaderFilterInputFieldErrorMessage(3), "EmsError happened while getting permissions");
        sa.assertNull(VaadinNotificationComponent.hasNotification(driver));

        sa.assertAll();
    }

    @Test
    public void databaseUnavailableWhenSavingUser() throws SQLException {
        String pass = RandomGenerator.generateRandomOnlyLetterString();
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Password", pass);
        withData.put("Password again", pass);

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoCreateFailedTestData testResult = userPage.doDatabaseNotAvailableWhenCreateTest(withData, spyDataSource, 1);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getNotificationWhenPerform()).contains("User saving failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseUnavailableWhenUpdateUser() throws SQLException {
        String pass = RandomGenerator.generateRandomOnlyLetterString();
        LinkedHashMap<String, Object> withData = new LinkedHashMap<>();
        withData.put("Password", pass);
        withData.put("Password again", pass);

        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        DoUpdateFailedTestData testResult = userPage.doDatabaseNotAvailableWhenUpdateTest(withData, withData, spyDataSource, 1);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertThat(testResult.getResult().getNotificationText()).contains("User modifying failed: Database error");
        assertEquals(0, testResult.getResult().getFailedFields().size());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @AfterClass
    public void afterClass() {
        resetUsers();
    }
}

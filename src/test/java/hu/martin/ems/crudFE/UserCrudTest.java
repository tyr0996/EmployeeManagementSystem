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
import hu.martin.ems.pages.core.component.VaadinSwitchComponent;
import hu.martin.ems.pages.core.dialog.ConfirmationDialog.UserStatusChangingConfirmationDialog;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.UserSaveOrUpdateDialog;
import hu.martin.ems.pages.core.doTestData.*;
import org.openqa.selenium.By;
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
    public void beforeMethod() {
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
        DoReadTestData testResult = userPage.doReadTest(true);

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber());
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber());
        assertNull(testResult.getNotificationWhenPerform());

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void userChangeStatusTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        ElementLocation location = userPage.getGrid().getRandomLocation();

        String[] data = userPage.getGrid().getDataFromRowLocation(location, true);

        VaadinSwitchComponent switchComponent = new VaadinSwitchComponent(driver, userPage.getGrid().getCellAsVaadinGridCellContent(location, 3), By.xpath("./switch"), 0);
        switchComponent.setStatus(!switchComponent.getStatus());

        UserStatusChangingConfirmationDialog dialog = new UserStatusChangingConfirmationDialog(driver);
        dialog.getEnterButtonComponent().click();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        if(data[3].equals("true")){
            assertThat(notification.getText()).contains("disabled successfully");
        }
        else{
            assertThat(notification.getText()).contains("enabled successfully");
        }
        notification.close();

        userPage.getGrid().waitForRefresh();

        userPage.getGrid().applyFilter(data);
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
        userPage.getGrid().resetFilter();
        data[3] = data[3].equals("true") ? "false" : "true";
        userPage.getGrid().applyFilter(data);
        assertEquals(1, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
    }

    @Test
    public void userChangeStatusRejectTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        ElementLocation location = userPage.getGrid().getRandomLocation();

        String[] data = userPage.getGrid().getDataFromRowLocation(location, true);

        VaadinSwitchComponent switchComponent = new VaadinSwitchComponent(driver, userPage.getGrid().getCellAsVaadinGridCellContent(location, 3), By.xpath("./switch"), 0);
        switchComponent.setStatus(!switchComponent.getStatus());

        UserStatusChangingConfirmationDialog dialog = new UserStatusChangingConfirmationDialog(driver);
        dialog.getRejectButtonComponent().click();

        userPage.getGrid().waitForRefresh();

        userPage.getGrid().applyFilter(data);
        assertEquals(1, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
        userPage.getGrid().resetFilter();
        data[3] = data[3].equals("true") ? "false" : "true";
        userPage.getGrid().applyFilter(data);
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
    }

    @Test
    public void userChangeStatusCancelTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        ElementLocation location = userPage.getGrid().getRandomLocation();

        String[] data = userPage.getGrid().getDataFromRowLocation(location, true);

        VaadinSwitchComponent switchComponent = new VaadinSwitchComponent(driver, userPage.getGrid().getCellAsVaadinGridCellContent(location, 3), By.xpath("./switch"), 0);
        switchComponent.setStatus(!switchComponent.getStatus());

        UserStatusChangingConfirmationDialog dialog = new UserStatusChangingConfirmationDialog(driver);
        dialog.getCancelButtonComponent().click();

        userPage.getGrid().waitForRefresh();

        userPage.getGrid().applyFilter(data);
        assertEquals(1, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
        userPage.getGrid().resetFilter();
        data[3] = data[3].equals("true") ? "false" : "true";
        userPage.getGrid().applyFilter(data);
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
    }

    @Test
    public void userChangeStatusDatabaseNotAvailableTest() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        ElementLocation location = userPage.getGrid().getRandomLocation();

        String[] data = userPage.getGrid().getDataFromRowLocation(location, true);

        VaadinSwitchComponent switchComponent = new VaadinSwitchComponent(driver, userPage.getGrid().getCellAsVaadinGridCellContent(location, 3), By.xpath("./switch"), 0);
        switchComponent.setStatus(!switchComponent.getStatus());

        UserStatusChangingConfirmationDialog dialog = new UserStatusChangingConfirmationDialog(driver);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        dialog.getEnterButtonComponent().click();

        userPage.getGrid().waitForRefresh();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        if(data[3].equals("true")){
            assertThat(notification.getText()).contains("disabling failed: Database error");
        }
        else{
            assertThat(notification.getText()).contains("enabling failed: Database error");
        }
        notification.close();

        userPage.getGrid().applyFilter(data);
        assertEquals(1, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
        userPage.getGrid().resetFilter();
        data[3] = data[3].equals("true") ? "false" : "true";
        userPage.getGrid().applyFilter(data);
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
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
        assertEquals(1, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
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
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
        userPage.getGrid().resetFilter();

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenGettingLoggedInUser() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableWhen(spyDataSource, Arrays.asList(0));
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        SoftAssert sa = new SoftAssert();

        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        sa.assertEquals(notification.getText(), "EmsError happened while getting the logged in user. Deletion and modification is disabled", "1-es");
        notification.close();

        UserPage userPage = new UserPage(driver, port);
        sa.assertNotEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()), 0, "2-es");
        for (int i = 0; i < userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()); i++) {
            sa.assertFalse(userPage.getGrid().getModifyButton(i).isEnabled(), "4-es");
            sa.assertFalse(userPage.getGrid().getDeleteButton(i).isEnabled(), "5-ös");
        }
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
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()), 0);
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()), 0);
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void userRestoreTest() {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);
        userPage.initWebElements();
        DoRestoreTestData testResult = userPage.doRestoreTest();

        assertEquals(testResult.getDeletedRowNumberAfterMethod(), testResult.getOriginalDeletedRowNumber() - 1);
        assertEquals(testResult.getNonDeletedRowNumberAfterMethod(), testResult.getOriginalNonDeletedRowNumber() + 1);
        assertThat(testResult.getNotificationWhenPerform()).contains("User restored: ");

        userPage = new UserPage(driver, port);
        userPage.getGrid().applyFilter(testResult.getResult().getRestoredData());
        userPage.getGrid().waitForRefresh();
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()), 0);
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()), 1);
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
        assertEquals(0, userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()));
        assertEquals(0, userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()));
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
        userPage.getGrid().applyFilter("Erzsi", "$2a$12$XGHOnxr5AyfmOoIjKEEP7.JXIXZgNiB53uf2AhbpwdAFztqi8FqCy", "Martin", "false");
        DoUpdateTestData testData = userPage.doUpdateTest(withData);
        userPage.getGrid().resetFilter();
        assertEquals(testData.getDeletedRowNumberAfterMethod(), testData.getOriginalDeletedRowNumber());
        assertEquals(testData.getNonDeletedRowNumberAfterMethod(), testData.getOriginalNonDeletedRowNumber());
        assertEquals(testData.getNotificationWhenPerform(), "Username already exists!");
        userPage.getGrid().applyFilter("Erzsi", "$2a$12$XGHOnxr5AyfmOoIjKEEP7.JXIXZgNiB53uf2AhbpwdAFztqi8FqCy", "Martin", "false");
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()), 1);
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()), 0);
        userPage.getGrid().resetFilter();

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
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()), 0);

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
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()), 1);
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()), 0);
        userPage.getGrid().resetFilter();

        userPage.getGrid().applyFilter(original);
        assertEquals(userPage.getGrid().getTotalNonDeletedRowNumber(userPage.getShowDeletedSwitch()), 0);
        assertEquals(userPage.getGrid().getTotalDeletedRowNumber(userPage.getShowDeletedSwitch()), 0);
        userPage.getGrid().resetFilter();

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenFindAllRole() throws SQLException {
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

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenFindAllRoleInFilter() throws SQLException {
        EmptyLoggedInVaadinPage loggedInPage =
                (EmptyLoggedInVaadinPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 1);
        loggedInPage.getSideMenu().navigate(SideMenu.ADMIN_MENU, SideMenu.USER_SUB_MENU);

        UserPage userPage = new UserPage(driver, port);

        SoftAssert sa = new SoftAssert();

        sa.assertFalse(userPage.getGrid().getHeaderFilterInputFields().get(2).isEnabled());
        sa.assertEquals(userPage.getGrid().getHeaderFilterInputFieldErrorMessage(2), "EmsError happened while getting permissions");
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

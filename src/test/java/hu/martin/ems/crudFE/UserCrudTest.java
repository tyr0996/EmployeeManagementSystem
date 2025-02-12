package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.JPAConfig;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;
    private static final String showDeletedCheckBoxXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = contentXpath + "/vaadin-grid";
    private static final String createButtonXpath = contentXpath + "/vaadin-horizontal-layout/vaadin-button";
    
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.USER_SUB_MENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "User", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    public void clearUsers() throws InterruptedException {
        dp.executeSQL("DELETE FROM loginuser");
        dp.executeSQL("INSERT INTO loginuser (id, deleted, username, passwordHash, role_role_id, enabled) VALUES ('1', '0', 'admin', '$2a$12$Ei2ntwIK/6lePBO2UecedetPpxxDee3kmxnkWTXZI.CiPb86vejHe', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1), true)");
        dp.executeSQL("INSERT INTO loginuser (id, deleted, username, passwordHash, role_role_id, enabled) VALUES ('2', '0', 'robi', '$2a$12$/LIbE6V8xP/2frZmSbe5.OSMyqiIbwQEau0nNsGk./P2PXP1M8BFi', (SELECT id as role_role_id FROM Role WHERE id = 2 LIMIT 1), true)");
        dp.executeSQL("INSERT INTO loginuser (id, deleted, username, passwordHash, role_role_id, enabled) VALUES ('3', '0', 'Erzsi', '$2a$12$4Eb.fZ748irmUDwJl1NueO6CjrVLFiP0E41qx3xsE6KAYxx00IfrG', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1), false)");
        logger.info("Admin user successfully recovered");
        Thread.sleep(1000);
    }

    @Test
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void userCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
        
    }

    @Test
    public void useReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
        
    }

    @Test
    public void userDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
        
    }

    @Test
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
//            mockDatabaseNotAvailable(getClass(), spyDataSource, 10);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
        
    }

    @Test
    public void userUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
        
    }

    @Test
    public void databaseNotAvailableWhenGettingLoggedInUser() throws InterruptedException, SQLException {
        mockDatabaseNotAvailable(getClass(), spyDataSource, 2);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Error happened while getting the logged in user. Deletion and modification is disabled");
    }

    @Test
    public void databaseNotAvailableWhenGettingAllUser() throws InterruptedException, SQLException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        mockDatabaseNotAvailable(getClass(), spyDataSource, 1);
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Getting users failed");
    }

    @Test
    public void userRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
        
    }

    @Test
    public void userPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
        
    }

    @Test
    public void createUserAllreadyExists() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "admin");
        crudTestingUtil.createTest(withData, "Username already exists!", false);
        
    }

    @Test
    public void modifyUserAllreadyExists() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "robi");
//        int users = countVisibleGridDataRows(gridXpath);
//        crudTestingUtil.createTest();

        applyFilter(gridXpath, "Erzsi", "$2a$12$4Eb.fZ748irmUDwJl1NueO6CjrVLFiP0E41qx3xsE6KAYxx00IfrG", "false");
        crudTestingUtil.updateTest(withData, "Username already exists!", false);
    }


    @Test
    public void createUserPasswordDoesntMatch() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.createTest(withData, "Passwords doesn't match!", false);
        
    }

    @Test
    public void updateUserEmptyPassword() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "");
        withData.put("Password again", "");
        crudTestingUtil.updateTest(withData, "Password is required!", false);
        
    }

    @Test
    public void updateUserPasswordDouesntMatch() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.updateTest(withData, "Passwords doesn't match!", false);
        
    }


    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
        
    }


    @Test
    public void finalAllWithDeletedUnexpectedResponse() throws InterruptedException {
        Mockito.doReturn(null).when(spyUserService).findAll(true); //ApiClient-ben.findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(500);
        checkNotificationText("Getting users failed");
        checkNoMoreNotificationsVisible();
        
    }

    @Test
    public void updateUserButUsernameNotChanged() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "robi");
        applyFilter(gridXpath, "robi", "", "", "");
        crudTestingUtil.updateTest(withData, null, true);
        checkNoMoreNotificationsVisible();
        
    }

    @Test
    public void findAllRoleUnexpectedResponse() throws InterruptedException {
        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(500);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Role", "Error happened while getting roles");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
        checkNoMoreNotificationsVisible();
        
    }

    @Test
    public void databaseUnavailableWhenGetAllUser() throws SQLException, InterruptedException {
        JPAConfig.resetCallIndex();
        mockDatabaseNotAvailable(getClass(), spyDataSource, 3);
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Getting users failed");
//        crudTestingUtil.databaseUnavailableWhenGetAllEntity(this.getClass(), spyDataSource, port, mainMenu, subMenu, "users");
    }

    @Test
    public void databaseUnavailableWhenSavingUser() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 1);
    }

    @Test
    public void databaseUnavailableWhenUpdateUser() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenUpdateEntity(spyDataSource, null, null, 1);
    }

    @AfterMethod
    public void destroy() throws InterruptedException {
        clearUsers();
    }
}

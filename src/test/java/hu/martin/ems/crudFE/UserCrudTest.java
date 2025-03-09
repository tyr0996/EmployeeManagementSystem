package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.JPAConfig;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.*;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.testng.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
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

    @BeforeMethod
    public void beforeMethod(){
        resetUsers();
    }

    @Test
    @Video
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void userCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
        
    }

    @Test
    @Video
    public void useReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
        
    }

    @Test
    @Video
    public void userDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
        
    }

    @Test
    @Video
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
//            mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 10);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
        
    }

    @Test
    @Video
    public void userUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
        
    }

    @Test
    @Video
    public void databaseNotAvailableWhenGettingLoggedInUser() throws InterruptedException, SQLException {
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 2);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Error happened while getting the logged in user. Deletion and modification is disabled");
    }

    @Test
    @Video
    public void databaseNotAvailableWhenGettingAllUser() throws InterruptedException, SQLException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 1);
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Getting users failed");
    }

    @Test
    @Video
    public void userRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
        
    }

    @Test
    @Video
    public void userPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
        
    }

    @Test
    @Video
    public void createUserAllreadyExists() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "admin");
        crudTestingUtil.createTest(withData, "Username already exists!", false);
        
    }

    @Test
    @Video
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
    @Video
    public void createUserPasswordDoesntMatch() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.createTest(withData, "Passwords doesn't match!", false);
        
    }

    @Test
    @Video
    public void updateUserEmptyPassword() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "");
        withData.put("Password again", "");
        crudTestingUtil.updateTest(withData, "Password is required!", false);
        
    }

    @Test
    @Video
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
    @Video
    public void finalAllWithDeletedUnexpectedResponse() throws InterruptedException, SQLException {
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
//        Mockito.doReturn(null).when(spyUserService).findAll(true); //ApiClient-ben.findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(500);
        checkNotificationText("Getting users failed");
        checkNoMoreNotificationsVisible();
        assertEquals(countVisibleGridDataRows(gridXpath), 0);
    }

    @Test
    @Video
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
    @Video
    public void databaseNotAvailableWhenFindAllRole() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(500);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Role", "Error happened while getting roles");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
        checkNoMoreNotificationsVisible();
    }

    @Test
    @Video
    public void databaseUnavailableWhenGetAllUser() throws SQLException, InterruptedException {
        JPAConfig.resetCallIndex();
        mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        checkNotificationText("Getting users failed");
//        crudTestingUtil.databaseUnavailableWhenGetAllEntity(this.getClass(), spyDataSource, port, mainMenu, subMenu, "users");
    }

    @Test
    @Video
    public void databaseUnavailableWhenSavingUser() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 1);
    }

    @Test
    @Video
    public void databaseUnavailableWhenUpdateUser() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenUpdateEntity(spyDataSource, null, null, 1);
    }

    @AfterClass
    public void afterClass() throws InterruptedException {
        resetUsers();
    }
}

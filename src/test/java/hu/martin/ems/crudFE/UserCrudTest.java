package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.JPAConfig;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.*;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

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

    private GridTestingUtil gridTestingUtil;

    

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(driver);
        crudTestingUtil = new CrudTestingUtil(gridTestingUtil, driver, "User", showDeletedCheckBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @BeforeMethod
    public void beforeMethod(){
        resetUsers();
    }

    @Test
    
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void userCreateTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
        
    }

    @Test
    
    public void useReadTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
        
    }

    @Test
    
    public void userDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
        
    }

    @Test
    
    public void databaseNotAvailableWhileDeleteTest() throws InterruptedException, SQLException {
//            gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 10);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseNotAvailableWhenDeleteTest(spyDataSource, "Internal Server Error");
        
    }

    @Test
    
    public void userUpdateTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
        
    }

    @Test
    
    public void databaseNotAvailableWhenGettingLoggedInUser() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 2);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.checkNotificationText("Error happened while getting the logged in user. Deletion and modification is disabled");
    }

    @Test
    
    public void databaseNotAvailableWhenGettingAllUser() throws InterruptedException, SQLException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 1);
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.checkNotificationText("Getting users failed");
    }

    @Test
    
    public void userRestoreTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
        
    }

    @Test
    
    public void userPermanentlyDeleteTest() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
        
    }

    @Test
    
    public void createUserAllreadyExists() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "admin");
        crudTestingUtil.createTest(withData, "Username already exists!", false);
        
    }

    @Test
    
    public void modifyUserAllreadyExists() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "robi");
//        int users = gridTestingUtil.countVisibleGridDataRows(gridXpath);
//        crudTestingUtil.createTest();

        gridTestingUtil.applyFilter(gridXpath, "Erzsi", "$2a$12$4Eb.fZ748irmUDwJl1NueO6CjrVLFiP0E41qx3xsE6KAYxx00IfrG", "false");
        crudTestingUtil.updateTest(withData, "Username already exists!", false);
    }


    @Test
    
    public void createUserPasswordDoesntMatch() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.createTest(withData, "Passwords doesn't match!", false);
        
    }

    @Test
    
    public void updateUserEmptyPassword() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
         gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "");
        withData.put("Password again", "");
        crudTestingUtil.updateTest(withData, "Password is required!", false);
        
    }

    @Test
    
    public void updateUserPasswordDouesntMatch() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.updateTest(withData, "Passwords doesn't match!", false);
        
    }


    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
        
    }


    @Test
    
    public void finalAllWithDeletedUnexpectedResponse() throws InterruptedException, SQLException {
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
//        Mockito.doReturn(null).when(spyUserService).findAll(true); //ApiClient-ben.findAllWithDeleted();
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(500);
        gridTestingUtil.checkNotificationText("Getting users failed");
        gridTestingUtil.checkNoMoreNotificationsVisible();
        assertEquals(gridTestingUtil.countVisibleGridDataRows(gridXpath), 0);
    }

    @Test
    
    public void updateUserButUsernameNotChanged() throws InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "robi");
        gridTestingUtil.applyFilter(gridXpath, "robi", "", "", "");
        crudTestingUtil.updateTest(withData, null, true);
        gridTestingUtil.checkNoMoreNotificationsVisible();
        
    }

    @Test
    
    public void databaseNotAvailableWhenFindAllRole() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 4);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        Thread.sleep(500);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Role", "Error happened while getting roles");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
        gridTestingUtil.checkNoMoreNotificationsVisible();
    }

    @Test
    
    public void databaseUnavailableWhenGetAllUser() throws SQLException, InterruptedException {
        JPAConfig.resetCallIndex();
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 3);
//        Mockito.doReturn(null).when(spyRoleService).findAll(false);
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        gridTestingUtil.checkNotificationText("Getting users failed");
//        crudTestingUtil.databaseUnavailableWhenGetAllEntity(this.getClass(), spyDataSource, port, mainMenu, subMenu, "users");
    }

    @Test
    
    public void databaseUnavailableWhenSavingUser() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(this, spyDataSource, null, null, 1);
    }

    @Test
    
    public void databaseUnavailableWhenUpdateUser() throws SQLException, InterruptedException {
        gridTestingUtil.loginWith(driver, port, "admin", "admin");
        gridTestingUtil.navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenUpdateEntity(spyDataSource, null, null, 1);
    }

    @AfterClass
    public void afterClass() {
        resetUsers();
    }
}

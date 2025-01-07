package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.User;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;
    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";
    
    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.USER_SUB_MENU;

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "User", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    public void clearUsers() throws InterruptedException {
        dp.executeSQL("DELETE FROM loginuser");
        dp.executeSQL("INSERT INTO loginuser (id, deleted, username, password, role_role_id) VALUES ('1', '0', 'admin', 'admin', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1))");
        logger.info("Admin user successfully recovered");
        Thread.sleep(1000);
    }

    @Test
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void userCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
        clearUsers();
    }

    @Test
    public void useReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
        clearUsers();
    }

    @Test
    public void userDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
        clearUsers();
    }

    @Test
    public void userUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
        clearUsers();
    }

    @Test
    public void userRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
        clearUsers();
    }

    @Test
    public void userPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
        clearUsers();
    }

    @Test
    public void createUserAllreadyExists() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "admin");
        crudTestingUtil.createTest(withData, "Username already exists!", false);
        clearUsers();
    }

    @Test
    public void apiSendInvalidStatusCodeWhenSave() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyUserApiClient).save(any(User.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
        clearUsers();
    }

    @Test
    public void apiSendInvalidStatusCodeWhenModify() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyUserApiClient).update(any(User.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
        clearUsers();
    }

    @Test
    public void modifyUserAllreadyExists() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "admin");
        int users = countVisibleGridDataRows(gridXpath);
        for(int i = 0; i < users; i++){
            getDeleteButton(gridXpath, 0).click();
            GridTestingUtil.closeNotification(100);
        }
        crudTestingUtil.createTest();
        crudTestingUtil.updateTest(withData, "Username already exists!", false);
        clearUsers();
    }


    @Test
    public void createUserPasswordDoesntMatch() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.createTest(withData, "Passwords doesn't match!", false);
        clearUsers();
    }

    @Test
    public void updateUserEmptyPassword() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "");
        withData.put("Password again", "");
        crudTestingUtil.updateTest(withData, "Password is required!", false);
        clearUsers();
    }

    @Test
    public void updateUserPasswordDouesntMatch() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.updateTest(withData, "Passwords doesn't match!", false);
        clearUsers();
    }


    //@Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
        clearUsers();
    }


    @Test
    public void finalAllWithDeletedUnexpectedResponse() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyUserApiClient).findAllWithDeleted();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(500);
        checkNotificationText("Getting users failed");
        checkNoMoreNotificationsVisible();
        clearUsers();
    }

    @Test
    public void updateUserButUsernameNotChanged() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "admin");
        crudTestingUtil.updateTest(withData, null, true);
        checkNoMoreNotificationsVisible();
        clearUsers();
    }

    @Test
    public void findAllRoleUnexpectedResponse() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).findAll();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        Thread.sleep(500);
        LinkedHashMap<String, String> failedFieldData = new LinkedHashMap<>();
        failedFieldData.put("Role", "Error happened while getting roles");

        crudTestingUtil.createUnexpectedResponseCodeWhileGettingData(null, failedFieldData);
        checkNoMoreNotificationsVisible();
        clearUsers();
    }

    @Test
    public void databaseUnavailableWhenGetAllUser() throws SQLException, InterruptedException {
        crudTestingUtil.databaseUnavailableWhenGetAllEntity(spyDataSource, port, mainMenu, subMenu, "users");
    }

    @Test
    public void databaseUnavailableWhenSavingUser() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenSaveEntity(spyDataSource, null, null, 1);
    }

    @Test
    public void databaseUnavailableWhenUpdateUser() throws SQLException, InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.databaseUnavailableWhenUpdateEntity(spyDataSource, null, null, 1);
    }
}

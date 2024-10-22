package hu.martin.ems.crudFE;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.User;
import hu.martin.ems.model.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.xml.crypto.Data;
import java.time.Duration;
import java.util.LinkedHashMap;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.mockito.ArgumentMatchers.any;
import static org.testng.AssertJUnit.assertEquals;

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
        dp.executeSQL("DELETE FROM loginuser WHERE id = 1");
        dp.executeSQL("INSERT INTO loginuser (id, deleted, username, password, role_role_id) VALUES ('1', '0', 'admin', 'admin', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1))");
        logger.info("Admin user successfully recovered");
        Thread.sleep(1000);
    }

    @Test
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void userCreateTest() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest();
    }

    @Test
    public void useReadTest() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.readTest();
    }

    @Test
    public void userDeleteTest() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void userUpdateTest() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest();
    }

    @Test
    public void userRestoreTest() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void userPermanentlyDeleteTest() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @Test
    public void createUserAllreadyExists() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "admin");
        crudTestingUtil.createTest(withData, "Username already exists!", false);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenSave() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyUserApiClient).save(any(User.class));
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenModify() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyUserApiClient).update(any(User.class));
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
    }

    @Test
    public void modifyUserAllreadyExists() throws InterruptedException {
        clearUsers();
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
    }


    @Test
    public void createUserPasswordDoesntMatch() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.createTest(withData, "Passwords doesn't match!", false);
    }

    @Test
    public void updateUserEmptyPassword() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "");
        withData.put("Password again", "");
        crudTestingUtil.updateTest(withData, "Password is required!", false);
    }

    @Test
    public void updateUserPasswordDouesntMatch() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.updateTest(withData, "Passwords doesn't match!", false);
    }


    @Test
    public void extraFilterInvalidValue() throws InterruptedException {
        clearUsers();
        TestingUtils.loginWith(driver, port, "admin", "admin");
         navigateMenu(mainMenu, subMenu);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }

    @Test
    public void createFailedTest() throws JsonProcessingException, InterruptedException {
        clearUsers();
        crudTestingUtil.createFailedTest(port, spyUserApiClient, mainMenu, subMenu);
    }

    @Test
    public void modifyFailedTest() throws JsonProcessingException, InterruptedException {
        clearUsers();
        crudTestingUtil.modifyFailedTest(port, spyUserApiClient, mainMenu, subMenu);
    }
}

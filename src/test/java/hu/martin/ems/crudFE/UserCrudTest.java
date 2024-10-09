package hu.martin.ems.crudFE;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.config.DataProvider;
import hu.martin.ems.core.model.User;
import hu.martin.ems.model.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
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

import static hu.martin.ems.base.GridTestingUtil.countElementResultsFromGridWithFilter;
import static hu.martin.ems.base.GridTestingUtil.navigateMenu;
import static org.testng.AssertJUnit.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserCrudTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;
    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";

    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "User", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @BeforeMethod
    public void clearUsers() throws InterruptedException {
        dp.executeSQL("DELETE FROM loginuser WHERE id = 1");
        dp.executeSQL("INSERT INTO loginuser (id, deleted, username, password, role_role_id) VALUES ('1', '0', 'admin', 'admin', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1))");
        logger.info("Admin user successfully recovered");
        Thread.sleep(1000);
    }

    @Test
    //@Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void userCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        crudTestingUtil.createTest();
    }

    @Test
    public void useReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        crudTestingUtil.readTest();
    }

    @Test
    public void userDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void userUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        crudTestingUtil.updateTest();
    }

    @Test
    public void userRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void userPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @Test
    public void userAllreadyExists() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Username", "admin");
        crudTestingUtil.createTest(withData, "Username already exists!", false);
    }

    @Test
    public void createUserPasswordDoesntMatch() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "asdf");
        withData.put("Password again", "asd");
        crudTestingUtil.createTest(withData, "Passwords doesn't match!", false);
    }

    @Test
    public void updateUserEmptyPassword() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        LinkedHashMap<String, String> withData = new LinkedHashMap<>();
        withData.put("Password", "");
        withData.put("Password again", "");
        crudTestingUtil.updateTest(withData, "Password is required!", false);
    }


    @Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.USER_SUB_MENU);
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }
}

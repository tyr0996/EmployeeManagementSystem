package hu.martin.ems.crudFE;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.NotificationCheck;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.User;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import hu.martin.ems.vaadin.api.RoleXPermissionApiClient;
import org.mockito.Mockito;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.findClickableElementWithXpathWithWaiting;
import static hu.martin.ems.base.GridTestingUtil.navigateMenu;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RoleTest extends BaseCrudTest {
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";

    private static final String rolesButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button[1]";
    private static final String permissionsButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button[2]";
    private static final String roleXPermisisonPairingButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/div/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button[3]";

    private static final String mainMenu = UIXpaths.ADMIN_MENU;
    private static final String subMenu = UIXpaths.ACESS_MANAGEMENT_SUBMENU;


    @BeforeClass
    public void setup() {
        crudTestingUtil = new CrudTestingUtil(driver, "Role", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void roleCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.createTest();
    }

    @Test
    public void roleReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.readTest();
        Thread.sleep(100);

    }

    @Test
    public void roleDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void roleUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.updateTest();
    }

    @Test
    public void roleRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void rolePermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @Test
    public void extraFilterInvalidValue() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        NotificationCheck nc = new NotificationCheck();
        nc.setAfterFillExtraDataFilter("Invalid json in extra data filter field!");
        crudTestingUtil.readTest(new String[0], "{invalid json}", true, nc);
    }

    @Test
    public void createFailedTest() throws JsonProcessingException, InterruptedException {
        crudTestingUtil.createFailedTest(port, spyRoleApiClient, mainMenu, subMenu, rolesButtonXPath);
    }

    @Test
    public void createFailedTestRoleXPermission() throws JsonProcessingException, InterruptedException {
        crudTestingUtil.createFailedTest(port, spyRoleXPermissionApiClient, mainMenu, subMenu, rolesButtonXPath);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenSave() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).save(any(Role.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenModify() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).update(any(Role.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenSaveRoleXPermission() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.createTest(null, "Not expected status-code in saving", false);
    }

    @Test
    public void apiSendInvalidStatusCodeWhenSaveRoleXPermissionUpdateRole() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleXPermissionApiClient).save(any(RoleXPermission.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(mainMenu, subMenu);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.updateTest(null, "Not expected status-code in modifying", false);
    }
}

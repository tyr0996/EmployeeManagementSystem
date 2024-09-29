package hu.martin.ems;

import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.findClickableElementWithXpathWithWaiting;
import static hu.martin.ems.base.GridTestingUtil.navigateMenu;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AccessManagementTest {

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;
    private Integer port;
    private static WebDriver driver;
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedChecBoxXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-vertical-layout/vaadin-grid";
    private static final String createButtonXpath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-vertical-layout/vaadin-horizontal-layout/vaadin-button";

    private static final String rolesButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-button[1]";
    private static final String permissionsButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-button[2]";
    private static final String roleXPermisisonPairingButtonXPath = "/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-button[3]";

    @BeforeEach
    public void setup() {
        port = webServerAppCtxt.getWebServer().getPort();
        driver = new ChromeDriver();
        crudTestingUtil = new CrudTestingUtil(driver, "Role", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void roleCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.createTest();
    }

    @Test
    public void roleReadTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        crudTestingUtil.readTest();
        Thread.sleep(100);

    }

    @Test
    public void roleDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void roleUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.updateTest();
    }

    @Test
    public void roleRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void rolePermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        findClickableElementWithXpathWithWaiting(rolesButtonXPath).click();
        Thread.sleep(100);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @AfterEach
    public void destroy() {
        driver.close();
    }

}

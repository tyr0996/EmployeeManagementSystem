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
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.navigateMenu;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SupplierCrudTest {
    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;
    private Integer port;
    private static WebDriver driver;
    private static CrudTestingUtil crudTestingUtil;
    private static WebDriverWait notificationDisappearWait;

    private static final String showDeletedChecBoxXpath = "//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid";
    private static final String createButtonXpath = "//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-button";

    @BeforeEach
    public void setup() {
        port = webServerAppCtxt.getWebServer().getPort();
        driver = new ChromeDriver();
        crudTestingUtil = new CrudTestingUtil(driver, "Supplier", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    @Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void supplierCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.SUPPLIER_SUBMENU);
        crudTestingUtil.createTest();
    }

    @Test
    public void supplierReadTest() {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.SUPPLIER_SUBMENU);
        crudTestingUtil.readTest();
    }

    @Test
    public void supplierDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.SUPPLIER_SUBMENU);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void supplierUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.SUPPLIER_SUBMENU);
        crudTestingUtil.updateTest();
    }

    @Test
    @Sql(scripts = {"file:src/test/java/hu/martin/ems/sql/addresses.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void supplierRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.SUPPLIER_SUBMENU);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void supplierPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.SUPPLIER_SUBMENU);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @AfterEach
    public void destroy() {
        driver.close();
    }
}

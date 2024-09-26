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

import static hu.martin.ems.base.GridTestingUtil.navigateMenu;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CityCrudTest {

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
        crudTestingUtil = new CrudTestingUtil(driver, "City", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void cityCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CITY_SUBMENU);
        crudTestingUtil.createTest();
    }

    @Test
    public void cityReadTest() {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CITY_SUBMENU);
        crudTestingUtil.readTest();
    }

    @Test
    public void cityDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CITY_SUBMENU);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void cityUpdateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CITY_SUBMENU);
        crudTestingUtil.updateTest();
    }

    @Test
    public void cityRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CITY_SUBMENU);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void cityPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.CITY_SUBMENU);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @AfterEach
    public void destroy() {
        driver.close();
    }
}

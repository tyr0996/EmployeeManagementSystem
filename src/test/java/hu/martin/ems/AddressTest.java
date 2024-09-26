package hu.martin.ems;

import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.navigateMenu;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class AddressTest {
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
        crudTestingUtil = new CrudTestingUtil(driver, "Address", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void addressCreateTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADDRESS_SUBMENU);
        crudTestingUtil.createTest();
    }

    @Test
    public void addressReadTest() {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADDRESS_SUBMENU);
        crudTestingUtil.readTest();
    }

    @Test
    public void addressDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADDRESS_SUBMENU);
        crudTestingUtil.deleteTest();
    }

    @Test
    public void addressUpdateTest() throws InterruptedException {
        for(int i = 0; i < 50; i++){
            TestingUtils.loginWith(driver, port, "admin", "admin");
            navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADDRESS_SUBMENU);
            crudTestingUtil.updateTest();
            System.out.println("Successfully runed " + (i+1) + " times");
        }
    }

    @Test
    public void addressRestoreTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADDRESS_SUBMENU);
        crudTestingUtil.restoreTest();
    }

    @Test
    public void addressPermanentlyDeleteTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.ADDRESS_SUBMENU);
        crudTestingUtil.permanentlyDeleteTest();
    }

    @AfterEach
    public void destroy() {
        driver.close();
    }
}

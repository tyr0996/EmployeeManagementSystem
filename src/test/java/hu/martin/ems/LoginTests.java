package hu.martin.ems;

import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.CrudTestingUtil;
import hu.martin.ems.base.RandomGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;

import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LoginTests {

    private static WebDriver driver;

    private static WebDriverWait notificationDisappearWait;

    private static CrudTestingUtil crudTestingUtil;

    private static final String showDeletedChecBoxXpath = "//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-checkbox";
    private static final String gridXpath = "//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-grid";
    private static final String createButtonXpath = "//*[@id=\"ROOT-2521314\"]/vaadin-horizontal-layout/vaadin-vertical-layout[2]/vaadin-horizontal-layout/vaadin-button";

    @Autowired
    private ServletWebServerApplicationContext webServerAppCtxt;

    private Integer port;

    @BeforeEach
    public void setup() {
        port = webServerAppCtxt.getWebServer().getPort();
        //serverPort.setPort(port);
        driver = new ChromeDriver();
        crudTestingUtil = new CrudTestingUtil(driver, "Employee", showDeletedChecBoxXpath, gridXpath, createButtonXpath);
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
    }

    @Test
    public void registrationSuccessButNoPermissionTest() throws InterruptedException {
        driver.get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper/vaadin-button[3]")));
        registerButton.click();

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field/input")));
        WebElement passwordField = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[1]/input");
        WebElement passwordAgainField = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[2]/input");
        WebElement register = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        String userName = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        usernameField.sendKeys(userName);
        passwordField.sendKeys(password);
        passwordAgainField.sendKeys(password);

        register.click();
        checkNotificationContainsTexts("Registration successful!");

        Thread.sleep(2000);
        loginWith(userName, password);
        Thread.sleep(2000);

        WebElement errorHeader = findVisibleElementWithXpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper//section/div[1]/h5");
        WebElement errorText = findVisibleElementWithXpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper//section/div[1]/p");

        assertEquals("Permission error", errorHeader.getText());
        assertEquals("You have no permission to log in. Contact the administrator about your roles, and try again!", errorText.getText());
    }

    @Test
    public void unauthorizedCredidentalsTest() throws InterruptedException {
        loginWith("unauthorized", "unauthorized");
        assertEquals("http://localhost:" + port + "/login", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");

        Thread.sleep(1000);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        //Valamiért hiába várom, hogy megtalálja az Error-t, egyszerűen nem találja meg. Bár lehet, hogy a h5 az ami nem tetszik neki
        WebElement errorHeader = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper//section/div[1]/h5")));
        WebElement errorText = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper//section/div[1]/p")));

        assertEquals("Incorrect username or password", errorHeader.getText());
        assertEquals("Check that you have entered the correct username and password and try again.", errorText.getText());
    }

    @Test
    public void forgotPassword_userNotFoundTest() throws InterruptedException {
        modifyPassword("notExistingUserName", "asdf", "asdf");
        checkNotificationContainsTexts("User not found!");
    }

    @Test
    public void forgotPassword_passwordDoesNotMatchAndUserNotFound() throws InterruptedException {
        modifyPassword("notExistingUserName", "asdf", "asd");
        checkNotificationContainsTexts("The passwords doesn't match!");
    }

    @Test
    public void forgotPassword_passwordDoesNotMatch() throws InterruptedException {
        modifyPassword("admin", "asdf", "asd");
        checkNotificationContainsTexts("The passwords doesn't match!");
    }

    private void modifyPassword(String userName, String password, String againPassword) throws InterruptedException {
        driver.get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"input-vaadin-text-field-6\"]")));

        WebElement forgotPasswordButton = driver.findElement(By.xpath("//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[2]"));
        WebElement registerButton = driver.findElement(By.xpath("//*[@id=\"ROOT-2521314\"]/vaadin-vertical-layout/vaadin-button"));

        forgotPasswordButton.click();
        Thread.sleep(500);
        WebElement forgotPasswordDialog = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout");
        WebElement usernameField = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field/input");
        WebElement nextButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        usernameField.sendKeys(userName);
        nextButton.click();

        Thread.sleep(500);
        WebElement forgotPasswordForDialog = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout");
        WebElement passwordField = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout/vaadin-password-field[1]/input");
        WebElement passwordAgainField = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout/vaadin-password-field[2]/input");
        WebElement submitButton = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout/vaadin-button");
        passwordField.sendKeys(password);
        passwordAgainField.sendKeys(againPassword);

        submitButton.click();
    }

    @Test
    public void forgotPassword_success() throws InterruptedException {
        modifyPassword("admin", "asdf", "asdf");
        checkNotificationContainsTexts("Password changed successfully!");
        Thread.sleep(2000);
        loginWith("admin", "asdf");
        Thread.sleep(2000);
        assertEquals("http://localhost:" + port + "/", driver.getCurrentUrl(), "Nem engedett be az új felhasználónév-jelszó párossal!");
        modifyPassword("admin", "admin", "admin");
        checkNotificationContainsTexts("Password changed successfully!");
    }

    @Test
    public void authorizedCredidentalsTest() throws InterruptedException {
        loginWith("admin", "admin");
        Thread.sleep(2000);
        assertEquals("http://localhost:" + port + "/", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
    }

    @Test
    public void sideMenuElementsTest() {
        loginWith("admin", "admin");
        findClickableElementWithXpath(UIXpaths.SIDE_MENU);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(1000));
        WebElement adminMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(UIXpaths.ADMIN_MENU)));
        WebElement ordersMenu = findClickableElementWithXpath(UIXpaths.ORDERS_MENU);

        assertEquals(false, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        adminMenu.click();
        assertEquals(true, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());
        
        adminMenu.click();
        assertEquals(false, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        ordersMenu.click();
        assertEquals(false, adminSubMenusVisible());
        assertEquals(true, ordersSubMenusVisible());

        ordersMenu.click();
        assertEquals(false, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        ordersMenu.click();
        adminMenu.click();
        assertEquals(true, adminSubMenusVisible());
        assertEquals(true, ordersSubMenusVisible());
    }

    @Test
    public void employeeCrudTest() throws InterruptedException {
        loginWith("admin", "admin");
        navigateMenu(UIXpaths.ADMIN_MENU, UIXpaths.EMPLOYEE_SUBMENU);

        //Create
        crudTestingUtil.createTest();

        //Read
        crudTestingUtil.readTest();

        //Delete

        crudTestingUtil.deleteTest();

        //update
        crudTestingUtil.updateTest();

        //restore
        crudTestingUtil.restoreTest();
    }

    private void loginWith(String username, String password) {
        driver.get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"input-vaadin-text-field-6\"]")));
        WebElement passwordField = driver.findElement(By.xpath("//*[@id=\"input-vaadin-password-field-7\"]"));
        WebElement loginButton = driver.findElement(By.xpath("//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[1]"));


        usernameField.sendKeys(username);
        passwordField.sendKeys(password);
        loginButton.click();
    }



    private boolean adminSubMenusVisible(){
        WebElement employeeSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.EMPLOYEE_SUBMENU);
        WebElement accessManagementSubMenu = findClickableElementWithXpath(UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        WebElement codestoreSubMenu = findClickableElementWithXpath(UIXpaths.CODESTORE_SUBMENU);
        WebElement citySubMenu = findClickableElementWithXpath(UIXpaths.CITY_SUBMENU);
        WebElement addressSubMenu = findClickableElementWithXpath(UIXpaths.ADDRESS_SUBMENU);
        WebElement customerSubMenu = findClickableElementWithXpath(UIXpaths.CUSTOMER_SUBMENU);
        WebElement productSubMenu = findClickableElementWithXpath(UIXpaths.PRODUCT_SUBMENU);
        WebElement supplierSubMenu = findClickableElementWithXpath(UIXpaths.SUPPLIER_SUBMENU);
        WebElement currencySubMenu = findClickableElementWithXpath(UIXpaths.CURRENCY_SUBMENU);
        WebElement usersSubMenu = findClickableElementWithXpath(UIXpaths.USER_SUB_MENU);

        return employeeSubMenu != null &&
                accessManagementSubMenu != null &&
                codestoreSubMenu != null &&
                citySubMenu != null &&
                addressSubMenu != null &&
                customerSubMenu != null &&
                productSubMenu != null &&
                supplierSubMenu != null &&
                currencySubMenu != null &&
                usersSubMenu != null;
    }
    
    private boolean ordersSubMenusVisible(){
        WebElement orderElementSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.ORDER_ELEMENT_SUBMENU);
        WebElement orderSubMenu = findClickableElementWithXpath(UIXpaths.ORDER_SUBMENU);
        WebElement orderCreateMenu = findClickableElementWithXpath(UIXpaths.ORDER_CREATE_SUBMENU);
        return orderElementSubMenu != null &&
                orderSubMenu != null &&
                orderCreateMenu != null;
    }

    @AfterEach
    public void destroy(){
        driver.close();
    }
}

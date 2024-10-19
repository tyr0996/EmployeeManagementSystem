package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.RandomGenerator;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.awt.*;
import java.time.Duration;
import java.util.ArrayList;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LoginTests extends BaseCrudTest {

    private static WebDriverWait notificationDisappearWait;

    private static String usernameFieldXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field/input";
    private static String passwordFieldXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[1]/input";



    @BeforeClass
    public void setup() {
        notificationDisappearWait = new WebDriverWait(driver, Duration.ofMillis(5000));
        GridTestingUtil.driver = driver;
    }

    @Test
    public void registrationSuccessButNoPermissionTest() throws InterruptedException {
        driver.get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper/vaadin-button[3]")));
        registerButton.click();

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(usernameFieldXpath)));
        WebElement passwordField = findClickableElementWithXpath(passwordFieldXpath);
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
        TestingUtils.loginWith(driver, port, userName, password);
        Thread.sleep(2000);

        checkLoginErrorMessage("Permission error",
                "You have no permission to log in. Contact the administrator about your roles, and try again.");
    }

    @Test
    public void registrationFailedPasswordDoesNotMatchTest() throws InterruptedException {
        driver.get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper/vaadin-button[3]")));
        registerButton.click();

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(usernameFieldXpath)));
        WebElement passwordField = findClickableElementWithXpath(passwordFieldXpath);
        WebElement passwordAgainField = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[2]/input");
        WebElement register = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        String userName = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        String otherPassword = RandomGenerator.generateRandomOnlyLetterString();
        usernameField.sendKeys(userName);
        passwordField.sendKeys(password);
        passwordAgainField.sendKeys(otherPassword);

        register.click();
        checkNotificationContainsTexts("The passwords doesn't match!");

        Thread.sleep(2000);
        TestingUtils.loginWith(driver, port, userName, password);
        Thread.sleep(2000);

        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    @Test
    public void registrationUsernameAllreadyExists() throws InterruptedException {
        driver.get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper/vaadin-button[3]")));
        registerButton.click();

        WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(usernameFieldXpath)));
        WebElement passwordField = findClickableElementWithXpath(passwordFieldXpath);
        WebElement passwordAgainField = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[2]/input");
        WebElement register = findClickableElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        String password = RandomGenerator.generateRandomOnlyLetterString();
        usernameField.sendKeys("admin");
        passwordField.sendKeys(password);
        passwordAgainField.sendKeys(password);

        register.click();
        checkNotificationContainsTexts("Username already exists!");

        Thread.sleep(2000);
        TestingUtils.loginWith(driver, port, "admin", password);
        Thread.sleep(2000);

        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    @Test
    public void unauthorizedCredidentalsTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "unauthorized", "unauthorized");
        assertEquals("http://localhost:" + port + "/login", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");

        Thread.sleep(1000);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
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
        WebElement registerButton = driver.findElement(By.xpath("//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[3]"));

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
        TestingUtils.loginWith(driver, port, "admin", "asdf");
        Thread.sleep(2000);
        assertEquals("http://localhost:" + port + "/", driver.getCurrentUrl(), "Nem engedett be az új felhasználónév-jelszó párossal!");
        modifyPassword("admin", "admin", "admin");
        checkNotificationContainsTexts("Password changed successfully!");
    }

    @Test
    public void authorizedCredidentalsTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        Thread.sleep(2000);
        assertEquals("http://localhost:" + port + "/", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
    }

    @Test
    public void logoutTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        Thread.sleep(10);

        WebElement logoutButton = findVisibleElementWithXpath("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout/vaadin-button");
        logoutButton.click();
        Thread.sleep(10);

        assertEquals("http://localhost:" + port + "/login", driver.getCurrentUrl(), "Nem történt meg a kijelentkeztetés");
    }

    @Test
    public void sideMenuElementsTest() {
        TestingUtils.loginWith(driver, port, "admin", "admin");
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
        WebElement admintoolsSubMenu = findClickableElementWithXpath(UIXpaths.ADMINTOOLS_SUB_MENU);

        return employeeSubMenu != null &&
                accessManagementSubMenu != null &&
                codestoreSubMenu != null &&
                citySubMenu != null &&
                addressSubMenu != null &&
                customerSubMenu != null &&
                productSubMenu != null &&
                supplierSubMenu != null &&
                currencySubMenu != null &&
                usersSubMenu != null &&
                admintoolsSubMenu != null;
    }
    
    private boolean ordersSubMenusVisible(){
        WebElement orderElementSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.ORDER_ELEMENT_SUBMENU);
        WebElement orderSubMenu = findClickableElementWithXpath(UIXpaths.ORDER_SUBMENU);
        WebElement orderCreateMenu = findClickableElementWithXpath(UIXpaths.ORDER_CREATE_SUBMENU);
        return orderElementSubMenu != null &&
                orderSubMenu != null &&
                orderCreateMenu != null;
    }

    private void checkLoginErrorMessage(String title, String description){
        WebElement login = findVisibleElementWithXpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper");
        SearchContext shadow = login.getShadowRoot();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement errorMessage = (WebElement) js.executeScript("return arguments[0].querySelector('div[part=\"error-message\"]');", shadow);


        String errorTitle = errorMessage.findElement(By.tagName("h5")).getText();
        String errorDescription = errorMessage.findElement(By.tagName("p")).getText();

        assertEquals(title, errorTitle, "Nem megfelelő a hibaüzenet címe");
        assertEquals(description, errorDescription, "Nem megfelelő a hibaüzenet leírás");
    }
}


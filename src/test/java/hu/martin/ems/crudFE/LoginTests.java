package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.core.model.EmsResponse;
import org.mockito.Mockito;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;

import static hu.martin.ems.base.GridTestingUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;


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

    private void register(String username, String password, String passwordAgain, String notification) throws InterruptedException {
        register(username, password, passwordAgain, notification, true);
    }

    private void register(String username, String password, String passwordAgain) throws InterruptedException {
        register(username, password, passwordAgain, "Registration successful!", true);
    }

    private void register(String username, String password, String passwordAgain, String notification, Boolean requireOpening) throws InterruptedException {
        driver.get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper/vaadin-button[3]")));
        registerButton.click();
        Thread.sleep(100);

        if(requireOpening){
            WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(usernameFieldXpath)));
            WebElement passwordField = findClickableElementWithXpathWithWaiting(passwordFieldXpath);
            WebElement passwordAgainField = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[2]/input");
            WebElement register = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

            usernameField.sendKeys(username);
            passwordField.sendKeys(password);
            passwordAgainField.sendKeys(passwordAgain);

            register.click();
        } else{
            assertThrows(TimeoutException.class, () -> wait.until(ExpectedConditions.elementToBeClickable(By.xpath(usernameFieldXpath))));
            assertEquals(null, findClickableElementWithXpathWithWaiting(passwordFieldXpath));
            assertEquals(null, findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[2]/input"));
            assertEquals(null, findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button"));
        }

        checkNotificationContainsTexts(notification);
    }

    @Test
    public void registrationSuccessButNoPermissionTest() throws InterruptedException {
        String userName = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        register(userName, password, password);

        Thread.sleep(200);
        TestingUtils.loginWith(driver, port, userName, password);
        Thread.sleep(200);

        checkLoginErrorMessage("Permission error",
                "You have no permission to log in. Contact the administrator about your roles, and try again.");
    }

    @Test
    public void registrationFailedPasswordDoesNotMatchTest() throws InterruptedException {

        String userName = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        String otherPassword = RandomGenerator.generateRandomOnlyLetterString();

        register(userName, password, otherPassword, "The passwords doesn't match!");

        Thread.sleep(200);
        TestingUtils.loginWith(driver, port, userName, password);
        Thread.sleep(200);

        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    @Test
    public void registrationUsernameAllreadyExists() throws InterruptedException {
        String password = RandomGenerator.generateRandomOnlyLetterString();
        register("admin", password, password, "Username already exists!");

        Thread.sleep(200);
        TestingUtils.loginWith(driver, port, "admin", password);
        Thread.sleep(200);

        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    @Test
    public void unauthorizedCredidentalsTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "unauthorized", "unauthorized");
        assertEquals("http://localhost:" + port + "/login", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");

        Thread.sleep(200);

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
        Thread.sleep(200);
        WebElement forgotPasswordDialog = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout");
        WebElement usernameField = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field/input");
        WebElement nextButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        usernameField.sendKeys(userName);
        nextButton.click();

        Thread.sleep(200);
        WebElement forgotPasswordForDialog = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout");
        WebElement passwordField = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout/vaadin-password-field[1]/input");
        WebElement passwordAgainField = findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout/vaadin-password-field[2]/input");
        WebElement submitButton = findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout/vaadin-button");
        passwordField.sendKeys(password);
        passwordAgainField.sendKeys(againPassword);

        submitButton.click();
    }

    @Test
    public void forgotPassword_success() throws InterruptedException {
        modifyPassword("admin", "asdf", "asdf");
        checkNotificationContainsTexts("Password changed successfully!");
        Thread.sleep(200);
        TestingUtils.loginWith(driver, port, "admin", "asdf");
        Thread.sleep(200);
        assertEquals("http://localhost:" + port + "/", driver.getCurrentUrl(), "Nem engedett be az új felhasználónév-jelszó párossal!");
        modifyPassword("admin", "admin", "admin");
        checkNotificationContainsTexts("Password changed successfully!");
    }

    @Test
    public void authorizedCredidentalsTest() throws InterruptedException {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        Thread.sleep(400);
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
    public void pageLoadFailedIllegalAccessException() throws IllegalAccessException, InterruptedException {
        Mockito.doThrow(IllegalAccessException.class).when(spyComponentManager).setEditObjectFieldToNull(any());
        TestingUtils.loginWith(driver, port, "admin", "admin");
        navigateMenu(UIXpaths.ORDERS_MENU, UIXpaths.ORDER_CREATE_SUBMENU);
        Thread.sleep(100);
        checkNotificationText("Error happened while load the clearing page!");
    }

    @Test
    public void sideMenuElementsTest() {
        TestingUtils.loginWith(driver, port, "admin", "admin");
        findClickableElementWithXpathWithWaiting(UIXpaths.SIDE_MENU);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(1000));
        WebElement adminMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(UIXpaths.ADMIN_MENU)));
        WebElement ordersMenu = findClickableElementWithXpathWithWaiting(UIXpaths.ORDERS_MENU);

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
    public void invalidStatusCodeWhenGettingAllRoles() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyRoleApiClient).findByName(any(String.class));
        String username = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        register(username, password, password, "Error happened while getting roles", false);
        Thread.sleep(10);
        TestingUtils.loginWith(driver, port, username, password);
        Thread.sleep(10);
        assertEquals("http://localhost:" + port + "/login", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
        Thread.sleep(300);
        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    @Test
    public void invalidStatusCodeWhenGettingUserByUsernameNewRegistrationTheRegistrationWasSuccess() throws InterruptedException {
        Mockito.doCallRealMethod().doReturn(new EmsResponse(522, "")).when(spyUserApiClient).findByUsername(any(String.class));
        String username = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        register(username, password, password);
        Thread.sleep(10);
        TestingUtils.loginWith(driver, port, username, password);
        Thread.sleep(10);
        assertEquals("http://localhost:" + port + "/login", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
        Thread.sleep(300);
        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    @Test
    public void invalidStatusCodeWhenGettingUserByUsernameNewRegistrationExistingUser() throws InterruptedException {
        Mockito.doReturn(new EmsResponse(522, "")).when(spyUserApiClient).findByUsername(any(String.class));
        TestingUtils.loginWith(driver, port, "admin", "admin");
        Thread.sleep(10);
        assertEquals("http://localhost:" + port + "/login", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
        Thread.sleep(300);
        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    private boolean adminSubMenusVisible(){
        WebElement employeeSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.EMPLOYEE_SUBMENU);
        WebElement accessManagementSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        WebElement codestoreSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.CODESTORE_SUBMENU);
        WebElement citySubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.CITY_SUBMENU);
        WebElement addressSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.ADDRESS_SUBMENU);
        WebElement customerSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.CUSTOMER_SUBMENU);
        WebElement productSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.PRODUCT_SUBMENU);
        WebElement supplierSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.SUPPLIER_SUBMENU);
        WebElement currencySubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.CURRENCY_SUBMENU);
        WebElement usersSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.USER_SUB_MENU);
        WebElement admintoolsSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.ADMINTOOLS_SUB_MENU);

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
        WebElement orderSubMenu = findClickableElementWithXpathWithWaiting(UIXpaths.ORDER_SUBMENU);
        WebElement orderCreateMenu = findClickableElementWithXpathWithWaiting(UIXpaths.ORDER_CREATE_SUBMENU);
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


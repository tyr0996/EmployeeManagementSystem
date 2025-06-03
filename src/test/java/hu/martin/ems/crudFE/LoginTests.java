package hu.martin.ems.crudFE;

import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.RandomGenerator;
import hu.martin.ems.base.mockito.MockingUtil;
import hu.martin.ems.pages.LoginPage;
import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.LoginErrorMessage;
import hu.martin.ems.pages.core.component.VaadinNotificationComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.time.Duration;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginTests extends BaseCrudTest {

    @BeforeMethod
    public void beforeMethod() {
        resetUsers();
    }

    @Test
    public void registrationSuccessButNoPermissionTest() {
        String userName = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();

        LoginPage loginPage = LoginPage.goToLoginPage(driver, port);
        loginPage.register(userName, password, password, true, true);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Registration successful!", notification.getText());
        notification.close();
        LoginErrorMessage error = ((LoginPage) loginPage.logIntoApplication(userName, password, false)).getErrorMessage();

        assertEquals("Permission error", error.getTitle());
        assertEquals("You have no permission to log in. Contact the administrator about your roles, and try again.", error.getDescription());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void registrationFailedPasswordDoesNotMatchTest() {

        String userName = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        String otherPassword = RandomGenerator.generateRandomOnlyLetterString();

        LoginPage loginPage = LoginPage.goToLoginPage(driver, port);
        loginPage.register(userName, password, otherPassword, true, true);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("The passwords doesn't match!", notification.getText());
        notification.close();

        LoginErrorMessage error = ((LoginPage) loginPage.logIntoApplication(userName, password, false)).getErrorMessage();

        assertEquals("Incorrect username or password", error.getTitle());
        assertEquals("Check that you have entered the correct username and password and try again.", error.getDescription());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void registrationUsernameAlreadyExists() {
        String password = RandomGenerator.generateRandomOnlyLetterString();

        LoginPage loginPage = LoginPage.goToLoginPage(driver, port);
        loginPage.register("admin", password, password, true, true);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Username already exists!", notification.getText());
        notification.close();

        LoginErrorMessage error = ((LoginPage) loginPage.logIntoApplication("admin", password, false)).getErrorMessage();

        assertEquals("Incorrect username or password", error.getTitle());
        assertEquals("Check that you have entered the correct username and password and try again.", error.getDescription());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void unauthorizedCredentialsTest() {
        LoginPage loginPage = LoginPage.goToLoginPage(driver, port);
        LoginErrorMessage error = ((LoginPage) loginPage.logIntoApplication("unauthorized", "unauthorized", false)).getErrorMessage();

        assertEquals("Incorrect username or password", error.getTitle());
        assertEquals("Check that you have entered the correct username and password and try again.", error.getDescription());
        assertEquals("http://localhost:" + port + "/login", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void forgotPassword_userNotFoundTest() {
        LoginPage loginPage = LoginPage.goToLoginPage(driver, port);
        loginPage.forgotPassword("notExistingUserName", "asdf", "asdf");
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("User not found!", notification.getText());
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test

    public void forgotPassword_passwordDoesNotMatchAndUserNotFound() {
        LoginPage loginPage = LoginPage.goToLoginPage(driver, port);
        loginPage.forgotPassword("notExistingUserName", "asdf", "asd");
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("The passwords doesn't match!", notification.getText());
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void forgotPassword_passwordDoesNotMatch() {
        LoginPage loginPage = LoginPage.goToLoginPage(driver, port);
        loginPage.forgotPassword("admin", "asdf", "asd");
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("The passwords doesn't match!", notification.getText());
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void forgotPassword_success() {
        LoginPage loginPage = LoginPage.goToLoginPage(driver, port);
        loginPage.forgotPassword("admin", "asdf", "asdf");
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Password changed successfully!", notification.getText());
        notification.close();
        loginPage.logIntoApplication("admin", "asdf", true);
        assertEquals("http://localhost:" + port + "/", driver.getCurrentUrl(), "Nem engedett be az új felhasználónév-jelszó párossal!");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void authorizedCredidentalsTest() {
        LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        assertEquals("http://localhost:" + port + "/", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void logoutTest() {
        LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        EmptyLoggedInVaadinPage loggedInPage = new EmptyLoggedInVaadinPage(driver, port);
        loggedInPage.logout();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void loggedOutUserTryReachPage() {
        LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        EmptyLoggedInVaadinPage loggedIn = new EmptyLoggedInVaadinPage(driver, port);
        loggedIn.logout();

        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void sideMenuElementsTest() {
        LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", true);
        EmptyLoggedInVaadinPage loggedInPage = new EmptyLoggedInVaadinPage(driver, port);

        Actions a = new Actions(driver);
        a.moveToElement(loggedInPage.getSideMenu().getElement()).perform();

        assertEquals(false, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        loggedInPage.getSideMenu().getAdminMenu().click();
        assertEquals(true, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        loggedInPage.getSideMenu().getAdminMenu().click();
        assertEquals(false, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        loggedInPage.getSideMenu().getOrdersMenu().click();
        assertEquals(false, adminSubMenusVisible());
        assertEquals(true, ordersSubMenusVisible());

        loggedInPage.getSideMenu().getOrdersMenu().click();
        assertEquals(false, adminSubMenusVisible());
        assertEquals(false, ordersSubMenusVisible());

        loggedInPage.getSideMenu().getOrdersMenu().click();
        loggedInPage.getSideMenu().getAdminMenu().click();
        assertEquals(true, adminSubMenusVisible());
        assertEquals(true, ordersSubMenusVisible());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test

    public void invalidStatusCodeWhenGettingAllRoles() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        String username = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        LoginPage loginPage = LoginPage.goToLoginPage(driver, port);
        loginPage.register(username, password, password, false, false);
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals("Database error", notification.getText());
        notification.close();

        loginPage.logIntoApplication(username, password, false);
        assertEquals("http://localhost:" + port + "/login", driver.getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
        assertEquals("Incorrect username or password", loginPage.getErrorMessage().getTitle());
        assertEquals("Check that you have entered the correct username and password and try again.", loginPage.getErrorMessage().getDescription());
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    @Test
    public void databaseNotAvailableWhenGettingUserByUsernameNewRegistrationExistingUser() throws SQLException {
        MockingUtil.mockDatabaseNotAvailableOnlyOnce(spyDataSource, 0);
        LoginPage failedLogin = (LoginPage) LoginPage.goToLoginPage(driver, port).logIntoApplication("admin", "29b{}'f<0V>Z", false);
        assertEquals("Incorrect username or password", failedLogin.getErrorMessage().getTitle());
        assertEquals("Check that you have entered the correct username and password and try again.", failedLogin.getErrorMessage().getDescription());
        VaadinNotificationComponent notification = new VaadinNotificationComponent(driver);
        assertEquals(notification.getText(), "EmsError happened while getting username");
        notification.close();
        assertNull(VaadinNotificationComponent.hasNotification(driver));
    }

    private boolean adminSubMenusVisible() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(200), Duration.ofMillis(5));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.EMPLOYEE_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.ACESS_MANAGEMENT_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.CODESTORE_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.CITY_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.ADDRESS_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.CUSTOMER_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.PRODUCT_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.SUPPLIER_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.CURRENCY_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.USER_SUB_MENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.ADMINTOOLS_SUB_MENU)));
            return true;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    private boolean ordersSubMenusVisible() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofMillis(200), Duration.ofMillis(5));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.ORDER_ELEMENT_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.ORDER_SUBMENU)));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(UIXpaths.ORDER_CREATE_SUBMENU)));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    @AfterClass
    public void afterClass() {
        resetUsers();
    }


}


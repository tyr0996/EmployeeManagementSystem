package hu.martin.ems.crudFE;

import com.automation.remarks.testng.UniversalVideoListener;
import com.automation.remarks.video.annotations.Video;
import hu.martin.ems.BaseCrudTest;
import hu.martin.ems.TestingUtils;
import hu.martin.ems.UITests.UIXpaths;
import hu.martin.ems.base.GridTestingUtil;
import hu.martin.ems.base.RandomGenerator;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.annotations.*;

import java.sql.SQLException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Listeners(UniversalVideoListener.class)
public class LoginTests extends BaseCrudTest {

    private static WebDriverWait notificationDisappearWait;

    private static String usernameFieldXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field/input";
    private static String passwordFieldXpath = "/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[1]/input";

    private GridTestingUtil gridTestingUtil;

    

    @BeforeClass
    public void setup() {
        gridTestingUtil = new GridTestingUtil(getDriver());
        notificationDisappearWait = new WebDriverWait(getDriver(), Duration.ofMillis(5000));
    }

    @BeforeMethod
    public void beforeMethod(){
        resetUsers();
    }

    private void register(String username, String password, String passwordAgain, String notification) throws InterruptedException {
        register(username, password, passwordAgain, notification, true);
    }

    private void register(String username, String password, String passwordAgain) throws InterruptedException {
        register(username, password, passwordAgain, "Registration successful!", true);
    }

    private void register(String username, String password, String passwordAgain, String notification, Boolean requireOpening) throws InterruptedException {
        getDriver().get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(2));

        WebElement registerButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper/vaadin-button[3]")));
        registerButton.click();
        Thread.sleep(100);

        if(requireOpening){
            WebElement usernameField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(usernameFieldXpath)));
            WebElement passwordField = gridTestingUtil.findClickableElementWithXpathWithWaiting(passwordFieldXpath);
            WebElement passwordAgainField = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[2]/input");
            WebElement register = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

            usernameField.sendKeys(username);
            passwordField.sendKeys(password);
            passwordAgainField.sendKeys(passwordAgain);

            register.click();
        } else{
//            assertThrows(TimeoutException.class, () -> wait.until(ExpectedConditions.elementToBeClickable(By.xpath(usernameFieldXpath))));
            assertEquals(null, gridTestingUtil.findClickableElementWithXpathWithWaiting(passwordFieldXpath));
            assertEquals(null, gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-password-field[2]/input"));
            assertEquals(null, gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button"));
        }

        gridTestingUtil.checkNotificationContainsTexts(notification);
    }

    @Test
    @Video
    public void registrationSuccessButNoPermissionTest() throws InterruptedException {
        String userName = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        register(userName, password, password);

        Thread.sleep(200);
        gridTestingUtil.loginWith(getDriver(), port, userName, password, false);
        Thread.sleep(200);

        checkLoginErrorMessage("Permission error",
                "You have no permission to log in. Contact the administrator about your roles, and try again.");
    }

    @Test
    @Video
    public void registrationFailedPasswordDoesNotMatchTest() throws InterruptedException {

        String userName = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        String otherPassword = RandomGenerator.generateRandomOnlyLetterString();

        register(userName, password, otherPassword, "The passwords doesn't match!");

        Thread.sleep(200);
        gridTestingUtil.loginWith(getDriver(), port, userName, password, false);
        Thread.sleep(200);

        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    @Test
    @Video
    public void registrationUsernameAllreadyExists() throws InterruptedException {
        String password = RandomGenerator.generateRandomOnlyLetterString();
        register("admin", password, password, "Username already exists!");

        Thread.sleep(500);
        gridTestingUtil.loginWith(getDriver(), port, "admin", password, false);
        Thread.sleep(200);

        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    @Test
    @Video
    public void unauthorizedCredidentalsTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "unauthorized", "unauthorized", false);
        assertEquals("http://localhost:" + port + "/login", getDriver().getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");

        Thread.sleep(200);

        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(2));

        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    @Test
    @Video
    public void forgotPassword_userNotFoundTest() throws InterruptedException {
        modifyPassword("notExistingUserName", "asdf", "asdf");
        gridTestingUtil.checkNotificationContainsTexts("User not found!");
    }

    @Test
    @Video
    public void forgotPassword_passwordDoesNotMatchAndUserNotFound() throws InterruptedException {
        modifyPassword("notExistingUserName", "asdf", "asd");
        gridTestingUtil.checkNotificationContainsTexts("The passwords doesn't match!");
    }

    @Test
    @Video
    public void forgotPassword_passwordDoesNotMatch() throws InterruptedException {
        modifyPassword("admin", "asdf", "asd");
        gridTestingUtil.checkNotificationContainsTexts("The passwords doesn't match!");
    }

    private void modifyPassword(String userName, String password, String againPassword) throws InterruptedException {
        getDriver().get("http://localhost:" + port + "/login");
        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(2));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"input-vaadin-text-field-6\"]")));

        WebElement forgotPasswordButton = getDriver().findElement(By.xpath("//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[2]"));
        WebElement registerButton = getDriver().findElement(By.xpath("//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[3]"));

        forgotPasswordButton.click();
        Thread.sleep(200);
        WebElement forgotPasswordDialog = gridTestingUtil.findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout");
        WebElement usernameField = gridTestingUtil.findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-text-field/input");
        WebElement nextButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay/vaadin-form-layout/vaadin-button");

        usernameField.sendKeys(userName);
        nextButton.click();

        Thread.sleep(200);
        WebElement forgotPasswordForDialog = gridTestingUtil.findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout");
        WebElement passwordField = gridTestingUtil.findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout/vaadin-password-field[1]/input");
        WebElement passwordAgainField = gridTestingUtil.findVisibleElementWithXpath("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout/vaadin-password-field[2]/input");
        WebElement submitButton = gridTestingUtil.findClickableElementWithXpathWithWaiting("/html/body/vaadin-dialog-overlay[2]/vaadin-form-layout/vaadin-button");
        passwordField.sendKeys(password);
        passwordAgainField.sendKeys(againPassword);

        submitButton.click();
    }

    @Test
    @Video
    public void forgotPassword_success() throws InterruptedException {
        modifyPassword("admin", "asdf", "asdf");
        gridTestingUtil.checkNotificationContainsTexts("Password changed successfully!");
        Thread.sleep(200);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "asdf");
        Thread.sleep(200);
        assertEquals("http://localhost:" + port + "/", getDriver().getCurrentUrl(), "Nem engedett be az új felhasználónév-jelszó párossal!");
    }

    @Test
    @Video
    public void authorizedCredidentalsTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        Thread.sleep(5000);
        assertEquals("http://localhost:" + port + "/", getDriver().getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
    }

    @Test
    @Video
    public void logoutTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        Thread.sleep(10);

        WebElement logoutButton = gridTestingUtil.findVisibleElementWithXpath("/html/body/div[1]/flow-container-root-2521314/vaadin-horizontal-layout/vaadin-vertical-layout/vaadin-button");
        logoutButton.click();
        Thread.sleep(100);

        assertEquals( true, getDriver().getCurrentUrl().contains("http://localhost:" + port + "/login"), "Nem történt meg a kijelentkeztetés");
    }



    @Test
    @Video
    public void sideMenuElementsTest() throws InterruptedException {
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin");
        gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.SIDE_MENU);

        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofMillis(1000));
        WebElement adminMenu = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(UIXpaths.ADMIN_MENU)));
        WebElement ordersMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.ORDERS_MENU);

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
    @Video
    public void invalidStatusCodeWhenGettingAllRoles() throws InterruptedException, SQLException {

//        Mockito.doReturn(null).when(spyRoleService).findByName(any(String.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(this, spyDataSource, 0);
        String username = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        register(username, password, password, "Error happened while getting no_role", false);
        Thread.sleep(10);
        gridTestingUtil.loginWith(getDriver(), port, username, password, false);
        Thread.sleep(10);
        assertEquals("http://localhost:" + port + "/login", getDriver().getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
        Thread.sleep(300);
        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }
    
    @Test
    @Video
    public void invalidStatusCodeWhenGettingUserByUsernameNewRegistrationTheRegistrationWasSuccess() throws InterruptedException {
//        Mockito.doCallRealMethod().doReturn(null).when(spyUserService).findByUsername(any(String.class));
        String username = RandomGenerator.generateRandomOnlyLetterString();
        String password = RandomGenerator.generateRandomOnlyLetterString();
        register(username, password, password);
        Thread.sleep(10);
        gridTestingUtil.loginWith(getDriver(), port, username, password, false);
        Thread.sleep(10);
        assertEquals("http://localhost:" + port + "/login", getDriver().getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
        Thread.sleep(300);
        checkLoginErrorMessage("Permission error",
                "You have no permission to log in. Contact the administrator about your roles, and try again.");
    }

    @Test
    @Video
    public void databaseNotAvailableWhenGettingUserByUsernameNewRegistrationExistingUser() throws InterruptedException, SQLException {
//        Mockito.doReturn(null).when(spyUserService).findByUsername(any(String.class));
        gridTestingUtil.mockDatabaseNotAvailableOnlyOnce(getClass(), spyDataSource, 0);
        gridTestingUtil.loginWith(getDriver(), port, "admin", "admin", false);
        Thread.sleep(10);
        assertEquals("http://localhost:" + port + "/login", getDriver().getCurrentUrl(), "Nem történt meg a megfelelő átirányítás");
        Thread.sleep(300);
        checkLoginErrorMessage("Incorrect username or password",
                "Check that you have entered the correct username and password and try again.");
    }

    private boolean adminSubMenusVisible(){
        WebElement employeeSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.EMPLOYEE_SUBMENU);
        WebElement accessManagementSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.ACESS_MANAGEMENT_SUBMENU);
        WebElement codestoreSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.CODESTORE_SUBMENU);
        WebElement citySubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.CITY_SUBMENU);
        WebElement addressSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.ADDRESS_SUBMENU);
        WebElement customerSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.CUSTOMER_SUBMENU);
        WebElement productSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.PRODUCT_SUBMENU);
        WebElement supplierSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.SUPPLIER_SUBMENU);
        WebElement currencySubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.CURRENCY_SUBMENU);
        WebElement usersSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.USER_SUB_MENU);
        WebElement admintoolsSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.ADMINTOOLS_SUB_MENU);

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
        WebElement orderElementSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.ORDER_ELEMENT_SUBMENU);
        WebElement orderSubMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.ORDER_SUBMENU);
        WebElement orderCreateMenu = gridTestingUtil.findClickableElementWithXpathWithWaiting(UIXpaths.ORDER_CREATE_SUBMENU);
        return orderElementSubMenu != null &&
                orderSubMenu != null &&
                orderCreateMenu != null;
    }

    private void checkLoginErrorMessage(String title, String description) throws InterruptedException {
        WebElement login = gridTestingUtil.findVisibleElementWithXpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper");
        SearchContext shadow = login.getShadowRoot();
        JavascriptExecutor js = (JavascriptExecutor) getDriver();

        WebDriverWait wait = new WebDriverWait(getDriver(), Duration.ofSeconds(10));

        WebElement errorMessage = (WebElement) js.executeScript("return arguments[0].querySelector('div[part=\"error-message\"]');", shadow);

        WebElement errorTitleElement = wait.until(ExpectedConditions.visibilityOf(errorMessage.findElement(By.tagName("h5"))));
        WebElement errorDescriptionElement = wait.until(ExpectedConditions.visibilityOf(errorMessage.findElement(By.tagName("p"))));

        String errorTitle = errorTitleElement.getText();
        String errorDescription = errorDescriptionElement.getText();

        assertEquals(title, errorTitle, "Nem megfelelő a hibaüzenet címe");
        assertEquals(description, errorDescription, "Nem megfelelő a hibaüzenet leírás");
    }

    @AfterClass
    public void afterClass(){
        resetUsers();
    }


}


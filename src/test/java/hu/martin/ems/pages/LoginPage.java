package hu.martin.ems.pages;

import hu.martin.ems.pages.core.EmptyLoggedInVaadinPage;
import hu.martin.ems.pages.core.LoginErrorMessage;
import hu.martin.ems.pages.core.VaadinPage;
import hu.martin.ems.pages.core.component.ForgotPasswordDialog_Password;
import hu.martin.ems.pages.core.component.ForgotPasswordDialog_Username;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends VaadinPage {

    private static final String userNameFieldXpath = "//*[@id=\"input-vaadin-text-field-6\"]";
    private static final String passwordFieldXpath = "//*[@id=\"input-vaadin-password-field-7\"]";
    private static final String loginButtonXpath = "//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[1]";
    private static final String registerButtonXpath = "//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[3]";
    private static final String forgotPasswordButtonXpath = "//*[@id=\"vaadinLoginFormWrapper\"]/vaadin-button[2]";

    //TODO megcsinálni normálisra ezeket
    private final WebElement userNameField;
    private final WebElement passwordField;
    @Getter private final WebElement loginButton;
    private final VaadinButtonComponent registerButton;
    @Getter private final VaadinButtonComponent forgotPasswordButton;

    private LoginPage(WebDriver driver, int port){
        super(driver, port);

        userNameField = getWait().until(ExpectedConditions.elementToBeClickable(By.xpath(userNameFieldXpath)));
        passwordField = getWait().until(ExpectedConditions.elementToBeClickable(By.xpath(passwordFieldXpath)));
        loginButton = getWait().until(ExpectedConditions.elementToBeClickable(By.xpath(loginButtonXpath)));
        forgotPasswordButton = new VaadinButtonComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(forgotPasswordButtonXpath))));
        registerButton = new VaadinButtonComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(registerButtonXpath))));
    }

    public void forgotPassword(String userName, String password, String passwordAgain){
        this.getForgotPasswordButton().click();
        ForgotPasswordDialog_Username fpd_1 = new ForgotPasswordDialog_Username(getDriver());
        fpd_1.getUsernameField().fillWith(userName);
        fpd_1.getNextButton().click();
        ForgotPasswordDialog_Password fpd_2 = new ForgotPasswordDialog_Password(getDriver());
        fpd_2.getPasswordField().fillWith(password);
        fpd_2.getPasswordAgainField().fillWith(passwordAgain);
        fpd_2.getSubmitButton().click();
    }

    public static LoginPage goToLoginPage(WebDriver driver, int port){
        driver.get("http://localhost:" + port + "/login");
        return new LoginPage(driver, port);
    }

    /**
     * @param userName Az a felhasználónév, amivel szeretnénk bejelentkezni
     * @param password A felhasználónévhez tartozó jelszó
     * @param requiredSuccess true, ha sikeres bejelentkezést várunk. False, ha nem.
     * @return ha sikeres a bejelentkezés, akkor egy EmptyLoggedInPage-t ad vissza, ha viszont sikertelen, akkor LoginPage-t.
     */
    public VaadinPage logIntoApplication(String userName, String password, Boolean requiredSuccess){
        userNameField.sendKeys(userName);
        passwordField.sendKeys(password);
        loginButton.click();


        return requiredSuccess ? new EmptyLoggedInVaadinPage(getDriver(), getPort()) : this;
    }

    public void register(String username, String password, String passwordAgain, Boolean requiredOpening, Boolean requiredClosing){
        registerButton.click();

        if(requiredOpening) {
            RegistrationDialog registrationDialog = new RegistrationDialog(getDriver());
            registrationDialog.initWebElements();
            registrationDialog.getUsernameField().fillWith(username);
            registrationDialog.getPasswordField().fillWith(password);
            registrationDialog.getPasswordAgainField().fillWith(passwordAgain);
            registrationDialog.getRegisterButton().click();
            if(requiredClosing){
                registrationDialog.close();
            }
        }
    }

    public LoginErrorMessage getErrorMessage(){
        WebElement login = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/vaadin-login-overlay-wrapper/vaadin-login-form/vaadin-login-form-wrapper")));
        SearchContext shadow = login.getShadowRoot();
        JavascriptExecutor js = (JavascriptExecutor) getDriver();

        WebElement errorMessage = (WebElement) js.executeScript("return arguments[0].querySelector('div[part=\"error-message\"]');", shadow);

        WebElement errorTitleElement = getWait().until(ExpectedConditions.visibilityOf(errorMessage.findElement(By.tagName("h5"))));
        WebElement errorDescriptionElement = getWait().until(ExpectedConditions.visibilityOf(errorMessage.findElement(By.tagName("p"))));

        String errorTitle = errorTitleElement.getText();
        String errorDescription = errorDescriptionElement.getText();

        return new LoginErrorMessage(errorTitle, errorDescription);
    }
}

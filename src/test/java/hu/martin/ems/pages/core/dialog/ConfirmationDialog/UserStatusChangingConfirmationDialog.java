package hu.martin.ems.pages.core.dialog.ConfirmationDialog;

import org.openqa.selenium.WebDriver;

public class UserStatusChangingConfirmationDialog extends VaadinConfirmationDialog {

    public UserStatusChangingConfirmationDialog(WebDriver driver) {
        super(driver, true, true);
    }

    @Override
    public void setAllComponents() {}
}

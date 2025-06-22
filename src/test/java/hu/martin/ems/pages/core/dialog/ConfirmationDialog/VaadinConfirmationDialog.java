package hu.martin.ems.pages.core.dialog.ConfirmationDialog;

import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinDialogComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public abstract class VaadinConfirmationDialog extends VaadinDialogComponent {

    @Getter
    private VaadinButtonComponent cancelButtonComponent;
    @Getter
    private VaadinButtonComponent rejectButtonComponent;
    @Getter
    private VaadinButtonComponent enterButtonComponent;

    private static final String cancelButtonComponentXpath = "/html/body/vaadin-confirm-dialog-overlay/vaadin-button[1]";
    private static final String enterButtonComponentXpath = "/html/body/vaadin-confirm-dialog-overlay/vaadin-button[2]";
    private static final String rejectButtonComponentXpath = "/html/body/vaadin-confirm-dialog-overlay/vaadin-button[3]";

    public VaadinConfirmationDialog(WebDriver driver, Boolean hasCancelButton, Boolean hasRejectButton) {
        super(driver, By.xpath("/html/body/vaadin-confirm-dialog-overlay"));

        initWebElements(hasCancelButton, hasRejectButton);
    }

    public void initWebElements(Boolean hasCancelButton, Boolean hasRejectButton){
        if(hasCancelButton){
            cancelButtonComponent = new VaadinButtonComponent(getDriver(), By.xpath(cancelButtonComponentXpath));
        }
        if(hasRejectButton){
            rejectButtonComponent = new VaadinButtonComponent(getDriver(), By.xpath(rejectButtonComponentXpath));
        }
        enterButtonComponent = new VaadinButtonComponent(getDriver(), By.xpath(enterButtonComponentXpath));
    }
}

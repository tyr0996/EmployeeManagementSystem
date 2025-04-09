package hu.martin.ems.pages.core.dialog.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinDialogComponent;
import org.openqa.selenium.WebDriver;

public abstract class VaadinSaveOrUpdateDialog extends VaadinDialogComponent implements IVaadinSaveOrUpdateDialog {
    public VaadinSaveOrUpdateDialog(WebDriver driver) {
        super(driver);
    }
}

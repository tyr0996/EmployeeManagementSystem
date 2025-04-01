package hu.martin.ems.pages.core.component.saveOrUpdateDialog;

import hu.martin.ems.pages.core.component.VaadinDropdownComponent;
import hu.martin.ems.pages.core.component.VaadinNumberInputComponent;
import hu.martin.ems.pages.core.component.VaadinTextInputComponent;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class EmployeeSaveOrUpdateDialog extends VaadinSaveOrUpdateDialog {

    private static final String firstNameInputComponentXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field[1]";
    private static final String lastNameInputComponentXpath = dialogXpath + "/vaadin-form-layout/vaadin-text-field[2]";
    private static final String salaryInputComponentXpath = dialogXpath + "/vaadin-form-layout/vaadin-number-field";
    private static final String userDropdownComponentXpath = dialogXpath + "/vaadin-form-layout/vaadin-combo-box";

    @Getter
    private VaadinTextInputComponent firstNameInputComponent;

    @Getter
    private VaadinTextInputComponent lastNameInputComponent;

    @Getter
    private VaadinNumberInputComponent salaryInputComponent;

    @Getter
    private VaadinDropdownComponent userDropdownComponent;


    public EmployeeSaveOrUpdateDialog(WebDriver driver) {
        super(driver);

//        initWebElements();
    }

    @Override
    public void initWebElements() {
        super.initWebElements();

        firstNameInputComponent = new VaadinTextInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(firstNameInputComponentXpath))));
        lastNameInputComponent = new VaadinTextInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(lastNameInputComponentXpath))));
        salaryInputComponent = new VaadinNumberInputComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(salaryInputComponentXpath))));
        userDropdownComponent = new VaadinDropdownComponent(getDriver(), getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(userDropdownComponentXpath))));

        setAllComponents();
    }

    @Override
    public void setAllComponents(){
        allComponent.add(firstNameInputComponent);
        allComponent.add(lastNameInputComponent);
        allComponent.add(salaryInputComponent);
        allComponent.add(userDropdownComponent);
    }
}

package hu.martin.ems.pages.core.component;

import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.MultiFillable;
import hu.martin.ems.pages.core.dialog.saveOrUpdateDialog.SingleFillable;
import jakarta.annotation.Nullable;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public abstract class VaadinDialogComponent extends VaadinBaseComponent implements IVaadinDialog {

    @Getter
    protected VaadinButtonComponent closeButton;
    @Getter
    protected VaadinButtonComponent saveButton;


    @Getter
    protected List<Fillable> allComponent = new ArrayList<>();

    protected static final String dialogXpath = "/html/body/vaadin-dialog-overlay";
    private static final String closeButtonXpath = dialogXpath + "/div/div/vaadin-button";

    public VaadinDialogComponent(WebDriver driver, By provider) {
        super(driver, provider);
    }

    public VaadinDialogComponent(WebDriver driver) {
        super(driver);
    }

    public void close() {
        getCloseButton().click();
        getWait().until(ExpectedConditions.invisibilityOf(element));
    }

    public void initWebElements() {
        element = getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(dialogXpath)));
        closeButton = new VaadinButtonComponent(getDriver(), By.xpath(dialogXpath + "/div/div/vaadin-button"));
        saveButton = new VaadinButtonComponent(getDriver(), By.xpath(dialogXpath + "/vaadin-form-layout/vaadin-button"));
    }


    public void fill(LinkedHashMap<String, Object> withData) {
        allComponent.forEach(v -> {
            if (withData != null && withData.containsKey(v.getTitle())) {
                fillWithData(v, withData.get(v.getTitle()));
            } else {
                fillWithRandomData(v, 1); //TODO jelenleg beégettem az 1-t. ide igazából bármennyit lehet. Csak valahogyan ki kellene szedni az elemből, amit szeretnék, hogy hány darab lehet a maximális érték itt, és akkor randommal meg lehet oldani.
            }
        });
    }

    public List<FailedVaadinFillableComponent> getFailedComponents() {
        return allComponent.stream().filter(v -> !v.isEnabled()).map(v -> new FailedVaadinFillableComponent(v)).toList();
    }


    private void fillWithData(Fillable fillable, Object withData) {
        if (fillable instanceof SingleFillable<?, ?>) {
            SingleFillable sf = (SingleFillable) fillable;
            sf.fillWith(withData);
        } else {
            MultiFillable mf = (MultiFillable) fillable;
            mf.fillWith(withData);
        }
    }

    private void fillWithRandomData(Fillable fillable, @Nullable Integer numberOfData) {
        if (fillable instanceof SingleFillable<?, ?>) {
            SingleFillable sf = (SingleFillable) fillable;
            sf.fillWithRandom();
        } else {
            MultiFillable mf = (MultiFillable) fillable;
            mf.fillWithRandom(numberOfData);
        }
    }
}

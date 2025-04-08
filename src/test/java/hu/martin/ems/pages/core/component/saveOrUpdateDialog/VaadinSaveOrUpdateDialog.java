package hu.martin.ems.pages.core.component.saveOrUpdateDialog;

import hu.martin.ems.pages.core.FailedVaadinFillableComponent;
import hu.martin.ems.pages.core.component.Fillable;
import hu.martin.ems.pages.core.component.VaadinButtonComponent;
import hu.martin.ems.pages.core.component.VaadinDialogComponent;
import jakarta.annotation.Nullable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.LinkedHashMap;
import java.util.List;

public abstract class VaadinSaveOrUpdateDialog extends VaadinDialogComponent implements IVaadinSaveOrUpdateDialog {


    public VaadinSaveOrUpdateDialog(WebDriver driver) {
        super(driver);
    }


    @Override
    public void initWebElements(){
        getWait().until(ExpectedConditions.visibilityOfElementLocated(By.xpath(dialogXpath)));
        closeButton = new VaadinButtonComponent(getDriver(), By.xpath(dialogXpath + "/div/div/vaadin-button"));
        saveButton = new VaadinButtonComponent(getDriver(), By.xpath(dialogXpath + "/vaadin-form-layout/vaadin-button"));
    }

    @Override
    public void close(){
        if(!this.isNull() && !closeButton.isNull()){
            closeButton.click();
            getWait().until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(dialogXpath)));
        }
    }

    @Override
    public void fill(LinkedHashMap<String, Object> withData) {
        allComponent.forEach(v -> {
            if (withData != null && withData.containsKey(v.getTitle())) {
                fillWithData(v, withData.get(v.getTitle()));
            } else {
                fillWithRandomData(v, 1); //TODO jelenleg beégettem az 1-t. ide igazából bármennyit lehet. Csak valahogyan ki kellene szedni az elemből, amit szeretnék, hogy hány darab lehet a maximális érték itt, és akkor randommal meg lehet oldani.
            }
        });
    }

    @Override
    public List<FailedVaadinFillableComponent> getFailedComponents(){
        return allComponent.stream().filter(v -> !v.isEnabled()).map(v -> new FailedVaadinFillableComponent(v)).toList();
    }


    private void fillWithData(Fillable fillable, Object withData){
        if(fillable instanceof SingleFillable<?,?>){
            SingleFillable sf = (SingleFillable) fillable;
            sf.fillWith(withData);
        }
        else{
            MultiFillable mf = (MultiFillable) fillable;
            mf.fillWith(withData);
        }
    }

    private void fillWithRandomData(Fillable fillable, @Nullable Integer numberOfData){
        if(fillable instanceof SingleFillable<?,?>){
            SingleFillable sf = (SingleFillable) fillable;
            sf.fillWithRandom();
        }
        else{
            MultiFillable mf = (MultiFillable) fillable;
            mf.fillWithRandom(numberOfData);
        }
    }
}

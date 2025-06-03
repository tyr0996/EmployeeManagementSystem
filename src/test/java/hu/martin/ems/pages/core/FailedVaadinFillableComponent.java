package hu.martin.ems.pages.core;

import hu.martin.ems.pages.core.component.Fillable;
import lombok.Getter;

@Getter
public class FailedVaadinFillableComponent<T extends Fillable> {
    private T failedComponent;
    private String errorMessage;
    private String fieldTitle;

    public FailedVaadinFillableComponent(T component) {
        assert !component.isEnabled() : "Cannot create FailedVaadinBaseComponent: component " + component.getTitle() + " isn't disabled!";
        this.failedComponent = component;
        this.errorMessage = component.getErrorMessage();
        this.fieldTitle = component.getTitle();
    }
}

package hu.martin.ems.pages.core.dialog.saveOrUpdateDialog;

import hu.martin.ems.pages.core.FailedVaadinFillableComponent;

import java.util.LinkedHashMap;
import java.util.List;

public interface IVaadinSaveOrUpdateDialog {
    void initWebElements();

    void close();

    void fill(LinkedHashMap<String, Object> withData);

    void setAllComponents();

    List<FailedVaadinFillableComponent> getFailedComponents();
}

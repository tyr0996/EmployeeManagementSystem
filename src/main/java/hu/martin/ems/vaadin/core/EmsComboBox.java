package hu.martin.ems.vaadin.core;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;
import java.util.function.Supplier;

public class EmsComboBox<T extends NamedObject> extends ComboBox<T> {
    public EmsComboBox(String label, Supplier<List<T>> setupFunction, Button saveButton, String errorMessage){
        this.setLabel(label);
        ComboBox.ItemFilter<T> filter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        List<T> data = setupFunction.get();
        if (data == null) {
            this.setInvalid(true);
            this.setErrorMessage(errorMessage);
            this.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            this.setInvalid(false);
            this.setEnabled(true);
            this.setItems(filter, data);
            this.setItemLabelGenerator(T::getName);
        }

    }
}

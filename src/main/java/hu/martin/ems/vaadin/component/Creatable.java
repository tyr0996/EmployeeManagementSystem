package hu.martin.ems.vaadin.component;

import com.vaadin.flow.component.dialog.Dialog;

public interface Creatable<T> {
    Dialog getSaveOrUpdateDialog(T entity);
}

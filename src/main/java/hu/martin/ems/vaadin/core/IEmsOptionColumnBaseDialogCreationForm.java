package hu.martin.ems.vaadin.core;

import com.vaadin.flow.component.dialog.Dialog;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.vaadin.component.BaseVO;

public interface IEmsOptionColumnBaseDialogCreationForm<S extends BaseEntity, T extends BaseVO<S>> extends IEmsOptionColumnBase<S, T> {

    EmsDialog getSaveOrUpdateDialog(T entity);

    default void editButtonClickEvent(T entity){
        Dialog dialog = getSaveOrUpdateDialog(entity);
        dialog.open();
    }
}

package hu.martin.ems.vaadin.core;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.core.vaadin.DownloadButton;
import hu.martin.ems.vaadin.component.BaseVOWithDeletable;

public interface IEmsOptionColumnBaseDialogCreationFormDeletable<S extends BaseEntity, T extends BaseVOWithDeletable<S>> extends IEmsOptionColumnBaseDialogCreationForm<S, T> {
    Dialog getSaveOrUpdateDialog(T entity);

    default void editButtonClickEvent(T entity){
        Dialog dialog = getSaveOrUpdateDialog(entity);
        dialog.open();
    }

    default HorizontalLayout createOptionColumn(String entityTypeName, T entity, GridButtonSettings buttonSettings, DownloadButton[] downloadButtons, Button[] bonusButtons){
        IconProvider iconProvider = BeanProvider.getBean(IconProvider.class);
        Button editButton = new Button(iconProvider.create(IconProvider.EDIT_ICON));
        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
        restoreButton.addClassNames("info_button_variant");
        Button permanentDeleteButton = new Button(iconProvider.create(IconProvider.PERMANENTLY_DELETE_ICON));
        permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        editButton.setEnabled(buttonSettings.getEditButtonEnabled());
        deleteButton.setEnabled(buttonSettings.getDeleteButtonEnabled());

        editButton.addClickListener(event -> editButtonClickEvent(entity));

        restoreButton.addClickListener(event -> restoreButtonClickEvent(entityTypeName, entity));

        deleteButton.addClickListener(event -> deleteButtonClickEvent(entityTypeName, entity));

        permanentDeleteButton.addClickListener(event -> permanentlyDeleteClickEvent(entityTypeName, entity));

        editButton.setEnabled(entity.deletable);
        deleteButton.setEnabled(entity.deletable);
        permanentDeleteButton.setEnabled(entity.deletable);
        restoreButton.setEnabled(entity.deletable);

        HorizontalLayout actions = new HorizontalLayout();
        if (entity.deleted == 0) {
            actions.add(editButton, deleteButton);
        } else {
            actions.add(permanentDeleteButton, restoreButton);
        }

//        if(downloadButtons != null){
//            for(int i = 0; i < downloadButtons.length; i++){
//                actions.add(downloadButtons[i]);
//            }
//        }
//        if(bonusButtons != null) {
//            for(int i = 0; i < bonusButtons.length; i++){
//                actions.add(bonusButtons);
//            }
//        }
        return actions;
    }
}

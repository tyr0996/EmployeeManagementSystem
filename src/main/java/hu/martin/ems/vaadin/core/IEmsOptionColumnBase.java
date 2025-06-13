package hu.martin.ems.vaadin.core;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.vaadin.DownloadButton;
import hu.martin.ems.vaadin.api.EmsApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import org.vaadin.klaudeta.PaginatedGrid;

public interface IEmsOptionColumnBase<S extends BaseEntity, T extends BaseVO<S>> {

    PaginatedGrid<T, String> getGrid();
    EmsApiClient<S> getApiClient();
    void setEntities();
    void updateGridItems();

    default HorizontalLayout createOptionColumn(String entityTypeName, T entity, GridButtonSettings buttonSettings){
        return createOptionColumn(entityTypeName, entity, buttonSettings, null, null);
    }
    default HorizontalLayout createOptionColumn(String entityTypeName, T entity){
        return createOptionColumn(entityTypeName, entity, new GridButtonSettings(true, true), null, null);
    }
    default HorizontalLayout createOptionColumn(String entityTypeName, T entity, Button[] bonusButtons){
        return createOptionColumn(entityTypeName, entity, new GridButtonSettings(true, true), null, bonusButtons);
    }
    default HorizontalLayout createOptionColumn(String entityTypeName, T entity, DownloadButton[] downloadButtons, Button[] bonusButtons){
        return createOptionColumn(entityTypeName, entity, new GridButtonSettings(true, true), downloadButtons, bonusButtons);
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

        editButton.setEnabled(buttonSettings.getEditButtonEnabled());
        deleteButton.setEnabled(buttonSettings.getDeleteButtonEnabled());
        permanentDeleteButton.setEnabled(buttonSettings.getDeleteButtonEnabled());
        restoreButton.setEnabled(buttonSettings.getEditButtonEnabled());

        HorizontalLayout actions = new HorizontalLayout();
        if (entity.deleted == 0) {
            actions.add(editButton, deleteButton);
        } else {
            actions.add(permanentDeleteButton, restoreButton);
        }

        if(downloadButtons != null){
            for (DownloadButton downloadButton : downloadButtons) {
                actions.add(downloadButton);
            }
        }
        if(bonusButtons != null) {
            for(Button bonusButton : bonusButtons){
                actions.add(bonusButton);
            }
        }
        return actions;
    }

    void editButtonClickEvent(T entity);

    default void restoreButtonClickEvent(String entityTypeName, T entity){
        EmsResponse response = this.getApiClient().restore(entity.original);
        switch (response.getCode()) {
            case 200:
                Notification.show(entityTypeName + " restored: " + entity.toString())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setEntities();
                updateGridItems();
                break;
            default: {
                Notification.show(response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }

    default void deleteButtonClickEvent(String entityTypeName, T entity){
        EmsResponse resp = getApiClient().delete(entity.original);
        switch (resp.getCode()) {
            case 200: {
                Notification.show(entityTypeName + " deleted: " + entity.toString())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
                break;
            }
            default: {
                Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
        setEntities();
        updateGridItems();
    }

    default void permanentlyDeleteClickEvent(String entityTypeName, T entity){
        EmsResponse response = getApiClient().permanentlyDelete(entity.original.id);
        switch (response.getCode()) {
            case 200: {
                Notification.show(entityTypeName + " permanently deleted: " + entity.toString())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setEntities();
                updateGridItems();
                break;
            }
            default: {
                Notification.show(entityTypeName + " permanently deletion failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        }
    }
}

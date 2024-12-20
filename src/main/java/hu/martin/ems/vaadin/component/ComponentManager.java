package hu.martin.ems.vaadin.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import hu.martin.ems.annotations.EditObject;
import hu.martin.ems.core.model.PaginationSetting;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class ComponentManager {

    @Autowired
    private PaginationSetting paginationSetting;

    @Autowired
    public ComponentManager(PaginationSetting paginationSetting){
        this.paginationSetting = paginationSetting;
    }

    private Logger logger = LoggerFactory.getLogger(ComponentManager.class);


    public void reloadComponent(Class listClass, Div contentLayout) {
        try {
            setEditObjectAnnotatedFieldToNull(listClass);
            com.vaadin.flow.component.Component newComponent = (com.vaadin.flow.component.Component) listClass.getDeclaredConstructor(PaginationSetting.class).newInstance(paginationSetting);

            UI.getCurrent().accessSynchronously(() -> {
                contentLayout.removeAll();
                contentLayout.add(newComponent);
            });
        } catch (Exception ex) {
            Notification.show("Error happened while load the clearing page!").addThemeVariants(NotificationVariant.LUMO_ERROR);
            ex.printStackTrace();
        }
    }

    public void setEditObjectAnnotatedFieldToNull(Class c) throws IllegalAccessException {
        Field editObjectField = getEditObjectField(c);
        if(editObjectField != null){
            setEditObjectFieldToNull(editObjectField);
        }
    }

    public Field getEditObjectField(Class c){
        return java.util.Arrays.stream(c.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(EditObject.class))
                .findFirst()
                .orElse(null);
    }

    public void setEditObjectFieldToNull(Field editObjectField) throws IllegalAccessException {
        editObjectField.set(null, null);
    }
}

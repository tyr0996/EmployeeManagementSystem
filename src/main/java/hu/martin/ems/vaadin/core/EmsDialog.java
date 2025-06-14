package hu.martin.ems.vaadin.core;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;

public class EmsDialog extends Dialog {

    public EmsDialog(String title) {
        super(title);
        addCloseButton();
    }

    public void addCloseButton() {
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> this.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        this.getHeader().add(closeButton);
    }
}

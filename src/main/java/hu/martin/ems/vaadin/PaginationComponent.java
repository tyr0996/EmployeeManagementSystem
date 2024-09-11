package hu.martin.ems.vaadin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class PaginationComponent extends HorizontalLayout {
    public PaginationComponent(){
        Button b = new Button("asdf");
        b.setVisible(true);
        add(b);
        this.setVisible(true);
    }
}

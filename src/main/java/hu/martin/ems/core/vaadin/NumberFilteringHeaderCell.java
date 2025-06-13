package hu.martin.ems.core.vaadin;

import com.vaadin.flow.component.textfield.NumberField;
import lombok.Getter;

public class NumberFilteringHeaderCell extends NumberField {

    @Getter
    private Double filterNumber;

    public NumberFilteringHeaderCell(String placeHolder, IEmsFilterableGridPage gridPage) {
        super(null, placeHolder);
        this.filterNumber = null;
        this.setClearButtonVisible(true);
        this.addValueChangeListener(v -> {
            filterNumber = v.getValue();
            gridPage.updateGridItems();
            gridPage.getGrid().getDataProvider().refreshAll();
        });
    }
}

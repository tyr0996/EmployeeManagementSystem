package hu.martin.ems.core.vaadin;


import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;

public class TextFilteringHeaderCell extends TextField {
    @Getter
    private String filterText;

    public TextFilteringHeaderCell(String placeHolder, IEmsFilterableGridPage gridPage){
        super(null, placeHolder);
        this.filterText = "";
        this.setClearButtonVisible(true);
        this.addValueChangeListener(v -> {
            filterText = v.getValue();
            gridPage.updateGridItems();
            gridPage.getGrid().getDataProvider().refreshAll();
        });
    }
}

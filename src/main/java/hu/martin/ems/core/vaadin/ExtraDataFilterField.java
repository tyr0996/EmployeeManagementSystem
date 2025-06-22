package hu.martin.ems.core.vaadin;

import com.google.gson.Gson;
import com.vaadin.flow.component.textfield.TextField;
import hu.martin.ems.core.config.BeanProvider;

import java.util.LinkedHashMap;
import java.util.List;

public class ExtraDataFilterField extends TextField {
    private String filterText;

    public LinkedHashMap<String, List<String>> getExtraDataFilterValue(){
        if(filterText.isEmpty()) {
            return new LinkedHashMap<>();
        }
        return BeanProvider.getBean(Gson.class).fromJson(filterText.trim(), LinkedHashMap.class);
    }

    public ExtraDataFilterField(String placeHolder, IEmsFilterableGridPage gridPage) {
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

package hu.martin.ems.vaadin.component.Currency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Currency;
import hu.martin.ems.service.CurrencyService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Route(value = "currency/list", layout = MainView.class)
public class CurrencyList extends VerticalLayout {

    private final CurrencyService currencyService;
    private boolean showDeleted = false;
    private Grid<CurrencyVO> grid;
    private final ObjectMapper om = new ObjectMapper();

    @Autowired
    public CurrencyList(CurrencyService currencyService) {
        this.currencyService = currencyService;

        this.grid = new Grid<>(CurrencyVO.class);

        Button fetch = new Button("Fetch currencies");
        fetch.addClickListener(event -> currencyService.fetchAndSaveRates());

        DatePicker datePicker = new DatePicker("Date");
        datePicker.setMax(LocalDate.now());
        datePicker.setValue(LocalDate.now());
        datePicker.addValueChangeListener(event -> updateGrid(datePicker));
        updateGrid(datePicker);
        add(fetch, datePicker, grid);
    }

    public void updateGrid(DatePicker dp) {
        LocalDate date = dp.getValue();
        Currency currency = currencyService.findByDate(date);

        if (currency == null) {
            if (date.isEqual(LocalDate.now())) {
                currency = currencyService.fetchAndSaveRates();
            } else {
                Notification.show("Az árfolyamokat nem lehet visszamenőlegesen letölteni!");
                dp.setValue(LocalDate.now());
                updateGrid(dp);
            }
        }
        List<CurrencyVO> data = new ArrayList<>();
        String baseCurrency = currency.getBaseCurrency().getName();
        try {
            LinkedHashMap<String, Double> map = om.readValue(currency.getRateJson(), LinkedHashMap.class);
            map.forEach((k, v) -> data.add(new CurrencyVO(k, v, baseCurrency)));
            this.grid.setItems(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    @Getter
    public class CurrencyVO {
        private String name;
        private String val;

        public CurrencyVO(String name, Double val, String baseCurrency) {
            this.name = name;
            this.val = val.toString() + " " + baseCurrency;
        }
    }
}

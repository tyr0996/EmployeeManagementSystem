package hu.martin.ems.vaadin.component.Currency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Currency;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CurrencyApi;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Route(value = "currency/list", layout = MainView.class)
@AnonymousAllowed
public class CurrencyList extends VerticalLayout {

    private final CurrencyApi currencyApi = BeanProvider.getBean(CurrencyApi.class);
    private boolean showDeleted = false;
    private PaginatedGrid<CurrencyVO, String> grid;
    private final ObjectMapper om = new ObjectMapper();
    private final PaginationSetting paginationSetting;

    @Autowired
    public CurrencyList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(CurrencyVO.class);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        Button fetch = new Button("Fetch currencies");
        fetch.addClickListener(event -> {
            Currency c = currencyApi.fetchAndSaveRates();
            Notification.show(c == null ? "Error happened while fetching exchange rates" :
                                            "Fetching exchange rates was successful!")
                    .addThemeVariants(c == null ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);
        });

        DatePicker datePicker = new DatePicker("Date");
        datePicker.setMax(LocalDate.now());
        datePicker.setValue(LocalDate.now());
        datePicker.addValueChangeListener(event -> updateGrid(datePicker));
        updateGrid(datePicker);
        add(fetch, datePicker, grid);
    }

    public void updateGrid(DatePicker dp) {
        LocalDate date = dp.getValue();
        Currency currency = currencyApi.findByDate(date);

        if (currency == null) {
            if (date.isEqual(LocalDate.now())) {
                currency = currencyApi.fetchAndSaveRates();
                if(currency == null){
                    Notification.show("Error happened while fetching exchange rates")
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    return;
                }
            } else {
                Notification.show("Exchange rates cannot be downloaded retroactively!")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
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

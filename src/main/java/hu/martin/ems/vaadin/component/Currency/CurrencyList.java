package hu.martin.ems.vaadin.component.Currency;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.date.DateUtil;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Currency;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CurrencyApiClient;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "currency/list", layout = MainView.class)
@RolesAllowed("ROLE_CurrencyMenuOpenPermission")
@NeedCleanCoding
public class CurrencyList extends EmsFilterableGridComponent {

    private final CurrencyApiClient currencyApiClient = BeanProvider.getBean(CurrencyApiClient.class);
    private boolean showDeleted = false;
    @Getter
    private PaginatedGrid<CurrencyVO, String> grid;

    private Gson gson = BeanProvider.getBean(Gson.class);

    List<CurrencyVO> currencyVOS;

    Grid.Column<CurrencyVO> nameColumn;
    Grid.Column<CurrencyVO> valColumn;
    private TextFilteringHeaderCell nameFilter;
    private TextFilteringHeaderCell valFilter;

    private DatePicker datePicker;
    Logger logger = LoggerFactory.getLogger(CurrencyList.class);
    private MainView mainView;

    @Autowired
    public CurrencyList(PaginationSetting paginationSetting) {
        this.mainView = mainView;

        this.grid = new PaginatedGrid<>(CurrencyVO.class);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        Button fetch = new Button("Fetch currencies");
        fetch.addClickListener(event -> {
            updateGrid(true);
        });

        datePicker = new DatePicker("Date");
        datePicker.setMax(LocalDate.now());
        datePicker.setValue(LocalDate.now());
        datePicker.addValueChangeListener(event -> updateGridItems());
        datePicker.setI18n(new DatePicker.DatePickerI18n().setDateFormats(
                "yyyy. MM. dd",
                DateUtil.generateAllFormats().toArray(new String[0])
        ));

        nameColumn = this.grid.addColumn(v -> v.name);
        valColumn = this.grid.addColumn(v -> v.val);

        setFilteringHeaderRow();

        updateGridItems();

        Anchor currencyApiAnchor = new Anchor("https://www.exchangerate-api.com", "Rates By Exchange Rate API");
        add(fetch, datePicker, grid, currencyApiAnchor);
    }

    public void updateGridItems() {
        updateGrid(false);
    }

    public void updateGrid(Boolean fetchButtonClicked) {
        LocalDate date = datePicker.getValue();
        EmsResponse response = currencyApiClient.findByDate(date);
        Currency currency;
        switch (response.getCode()) {
            case 200:
                currency = (Currency) response.getResponseData();
                break;
            default:
                logger.error("Currency findByDateError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                Notification.show("EmsError happened while getting currencies by date")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
        }
        Boolean needFetch = currency == null;
        if (!needFetch && fetchButtonClicked) {
            Notification.show("Currencies already fetched").addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        }

        if (needFetch) {
            if (date.isEqual(LocalDate.now())) {
                EmsResponse fetchAndSaveResponse = currencyApiClient.fetchAndSaveRates();
                switch (fetchAndSaveResponse.getCode()) {
                    case 200:
                        currency = gson.fromJson((String) fetchAndSaveResponse.getResponseData(), Currency.class);
                        break;
                    default: {
                        Notification.show(fetchAndSaveResponse.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                }
            } else {
                Notification.show("Exchange rates cannot be downloaded retroactively!")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                datePicker.setValue(LocalDate.now());
                updateGridItems();
                return;
            }
        }

        currencyVOS = new ArrayList<>();
        String baseCurrency = currency.getBaseCurrency().getName();

        LinkedTreeMap<String, Number> map = gson.fromJson(currency.getRateJson(), LinkedTreeMap.class);
        map.forEach((k, v) -> currencyVOS.add(new CurrencyVO(k, v, baseCurrency)));

        List<CurrencyVO> curr = getFilteredStream().collect(Collectors.toList());
        if (needFetch) {
            Notification.show("Fetching exchange rates was successful!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        this.grid.setItems(curr);
    }

    private Stream<CurrencyVO> getFilteredStream() {
        return currencyVOS.stream().filter(currencyVO ->
                filterField(valFilter, currencyVO.val) &&
                filterField(nameFilter, currencyVO.name));
    }


    private void setFilteringHeaderRow() {
        nameFilter = new TextFilteringHeaderCell("Search name...", this);
        valFilter = new TextFilteringHeaderCell("Search val...", this);

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(valColumn).setComponent(styleFilterField(valFilter, "Val"));
        filterRow.getCell(nameColumn).setComponent(styleFilterField(nameFilter, "Name"));
    }

    @NeedCleanCoding
    public class CurrencyVO {
        private String name;
        private String val;

        public CurrencyVO(String name, Number val, String baseCurrency) {
            this.name = name;
            this.val = new DecimalFormat("#.###").format(1.0 / val.doubleValue()) + " " + baseCurrency;
        }
    }
}

package hu.martin.ems.vaadin.component.Currency;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.date.Date;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Currency;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CurrencyApiClient;
import jakarta.annotation.security.RolesAllowed;
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
//@AnonymousAllowed
@RolesAllowed("ROLE_CurrencyMenuOpenPermission")
@NeedCleanCoding
//@RolesAllowed("CurrencyMenuOpenPermission")
public class CurrencyList extends VerticalLayout {

    private final CurrencyApiClient currencyApiClient = BeanProvider.getBean(CurrencyApiClient.class);
    private boolean showDeleted = false;
    private PaginatedGrid<CurrencyVO, String> grid;

    private Gson gson = BeanProvider.getBean(Gson.class);
    private final PaginationSetting paginationSetting;

    List<Currency> currencies;
    List<CurrencyVO> currencyVOS;

    Grid.Column<CurrencyVO> nameColumn;
    Grid.Column<CurrencyVO> valColumn;
    private static String nameFilterText = "";
    private static String valFilterText = "";

    private DatePicker datePicker;
    Logger logger = LoggerFactory.getLogger(CurrencyList.class);
    private MainView mainView;

    @Autowired
    public CurrencyList(PaginationSetting paginationSetting) {
        this.mainView = mainView;
        this.paginationSetting = paginationSetting;

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
        datePicker.addValueChangeListener(event -> updateGrid());
        datePicker.setI18n(new DatePicker.DatePickerI18n().setDateFormats(
                "yyyy. MM. dd",
                Date.generateAllFormats().toArray(new String[0])
        ));


        nameColumn = this.grid.addColumn(v -> v.name);
        valColumn = this.grid.addColumn(v -> v.val);

        setFilteringHeaderRow();

        updateGrid();
        add(fetch, datePicker, grid);
    }

    public void updateGrid(){
        updateGrid(false);
    }

    public void updateGrid(Boolean fetchButtonClicked) {
        LocalDate date = datePicker.getValue();
        Currency currency = getCurrencyByDate(date);
        Boolean needFetch = currency == null;
        if(!needFetch && fetchButtonClicked){
            Notification.show("Currencies already fetched").addThemeVariants(NotificationVariant.LUMO_PRIMARY);
        }

        if (needFetch) {
            if (date.isEqual(LocalDate.now())) {
                EmsResponse response = currencyApiClient.fetchAndSaveRates();
                switch (response.getCode()) {
                    case 200:
                        currency = gson.fromJson((String) response.getResponseData(), Currency.class);
                        break;
                    default: {
//                        Notification.show("Error happened while fetching exchange rates: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        Notification.show(response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                        return;
                    }
                }
            } else {
                Notification.show("Exchange rates cannot be downloaded retroactively!")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                datePicker.setValue(LocalDate.now());
                updateGrid();
                return;
            }
//        }
//        else if (fetchButtonClicked){
//
        }

        currencyVOS = new ArrayList<>();
        String baseCurrency = currency.getBaseCurrency().getName();

        LinkedTreeMap<String, Number> map = gson.fromJson(currency.getRateJson(), LinkedTreeMap.class);
        map.forEach((k, v) -> currencyVOS.add(new CurrencyVO(k, v, baseCurrency)));

        List<CurrencyVO> curr = getFilteredStream().collect(Collectors.toList());
        if(needFetch){
            Notification.show("Fetching exchange rates was successful!").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
        this.grid.setItems(curr);
    }

    private Currency getCurrencyByDate(LocalDate date) {
        EmsResponse response = currencyApiClient.findByDate(date);
        switch (response.getCode()){
            case 200:
                return (Currency) response.getResponseData();
            default:
                logger.error("Currency findByDateError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                Notification.show("Error happened while getting currencies by date")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return null;
        }
    }

    private Stream<CurrencyVO> getFilteredStream() {
        return currencyVOS.stream().filter(currencyVO ->
                (valFilterText.isEmpty() || currencyVO.val.toLowerCase().contains(valFilterText.toLowerCase())) &&
                (nameFilterText.isEmpty() || currencyVO.name.toLowerCase().contains(nameFilterText.toLowerCase()))
        );
    }

    private Component filterField(TextField filterField, String title){
        VerticalLayout res = new VerticalLayout();
        res.getStyle().set("padding", "0px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");
        filterField.getStyle().set("display", "flex").set("width", "100%");
        NativeLabel titleLabel = new NativeLabel(title);
        res.add(titleLabel, filterField);
        res.setClassName("vaadin-header-cell-content");
        return res;
    }

    private void setFilteringHeaderRow(){
        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Search name...");
        nameFilter.setClearButtonVisible(true);
        nameFilter.addValueChangeListener(event -> {
            nameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGrid();
        });

        TextField valFilter = new TextField();
        valFilter.setPlaceholder("Search val...");
        valFilter.setClearButtonVisible(true);
        valFilter.addValueChangeListener(event -> {
            valFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGrid();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(valColumn).setComponent(filterField(valFilter, "Val"));
        filterRow.getCell(nameColumn).setComponent(filterField(nameFilter, "Name"));
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

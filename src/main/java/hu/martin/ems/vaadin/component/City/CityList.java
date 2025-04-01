package hu.martin.ems.vaadin.component.City;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CityApiClient;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "city/list", layout = MainView.class)
@RolesAllowed("ROLE_CityMenuOpenPermission")
@NeedCleanCoding
public class CityList extends VerticalLayout implements Creatable<City> {

    private final CityApiClient cityApi = BeanProvider.getBean(CityApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private boolean showDeleted = false;
    private PaginatedGrid<CityVO, String> grid;
    private PaginationSetting paginationSetting;

    List<CityVO> cityVOS;
    List<City> cityList;
    List<CodeStore> countries;

    private String countryCodeFilterText = "";
    private String nameFilterText = "";
    private String zipCodeFilterText = "";
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<CityVO> extraData;

    private Grid.Column<CityVO> countryCodeColumn;
    private Grid.Column<CityVO> nameColumn;
    private Grid.Column<CityVO> zipCodeColumn;
    private Logger logger = LoggerFactory.getLogger(City.class);
    private Gson gson = BeanProvider.getBean(Gson.class);
    private MainView mainView;

    @Autowired
    public CityList(PaginationSetting paginationSetting) {
        createCityList(paginationSetting);
    }


    private void createCityList(PaginationSetting paginationSetting){
        this.paginationSetting = paginationSetting;

        CityVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(CityVO.class);
        setupCountries();
        setupCities();
        updateGridItems();

        countryCodeColumn = grid.addColumn(v -> v.countryCode);
        nameColumn = grid.addColumn(v -> v.name);
        zipCodeColumn = grid.addColumn(v -> v.zipCode);

        grid.addClassName("styling");
        grid.setPartNameGenerator(cityVO -> cityVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        extraData = this.grid.addComponentColumn(city -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog d = getSaveOrUpdateDialog(city.original);
                d.open();
            });

            restoreButton.addClickListener(event -> {
                this.cityApi.restore(city.original);
                Notification.show("City restored: " + city.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupCities();
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.cityApi.delete(city.original);
                switch (resp.getCode()){
                    case 200: {
                        Notification.show("City deleted: " + city.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setupCities();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                EmsResponse response = this.cityApi.permanentlyDelete(city.original.getId());
                switch (response.getCode()){
                    case 200: {
                        Notification.show("City permanently deleted: " + city.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        setupCities();
                        updateGridItems();
                    }
                    default: {
                        Notification.show("City permanently deletion failed: " + response.getDescription());
                    }
                }

            });

            HorizontalLayout actions = new HorizontalLayout();
            if (city.original.getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });

        setFilteringHeaderRow();

        //endregion

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            CityVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    private void setupCities() {
        EmsResponse response = cityApi.findAllWithDeleted();
        switch (response.getCode()){
            case 200:
                cityList = (List<City>) response.getResponseData();
                break;
            default:
                cityList = null;
                logger.error("City findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private Stream<CityVO> getFilteredStream() {
        return cityVOS.stream().filter(cityVO ->
                (zipCodeFilterText.isEmpty() || cityVO.zipCode.toLowerCase().contains(zipCodeFilterText.toLowerCase())) &&
                        (countryCodeFilterText.isEmpty() || cityVO.countryCode.toLowerCase().contains(countryCodeFilterText.toLowerCase())) &&
                        (nameFilterText.isEmpty() || cityVO.name.toLowerCase().contains(nameFilterText.toLowerCase())) &&
                        cityVO.filterExtraData()
                        //(showDeleted ? (cityVO.deleted == 0 || cityVO.deleted == 1) : cityVO.deleted == 0)
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
        TextField countryCodeFilter = new TextField();
        countryCodeFilter.setPlaceholder("Search country code...");
        countryCodeFilter.setClearButtonVisible(true);
        countryCodeFilter.addValueChangeListener(event -> {
            countryCodeFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField nameFilter = new TextField();
        nameFilter.setPlaceholder("Search name...");
        nameFilter.setClearButtonVisible(true);
        nameFilter.addValueChangeListener(event -> {
            nameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField zipCodeFilter = new TextField();
        zipCodeFilter.setPlaceholder("Search zip code...");
        zipCodeFilter.setClearButtonVisible(true);
        zipCodeFilter.addValueChangeListener(event -> {
            zipCodeFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                CityVO.extraDataFilterMap.clear();
            }
            else{
                CityVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        //extraDataFilter.setVisible(false);


        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(countryCodeColumn).setComponent(filterField(countryCodeFilter, "Country code"));
        filterRow.getCell(nameColumn).setComponent(filterField(nameFilter, "Name"));
        filterRow.getCell(zipCodeColumn).setComponent(filterField(zipCodeFilter, "ZipCode"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }


    private void updateGridItems() {
        if(cityList != null){
            cityVOS = cityList.stream().map(CityVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
        else {
            Notification.show("Getting cities failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void appendCloseButton(Dialog d){
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> d.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        d.getHeader().add(closeButton);
    }

    public Dialog getSaveOrUpdateDialog(City entity){
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " city");
        appendCloseButton(createDialog);

        Button saveButton = new Button("Save");
        FormLayout fl = new FormLayout();
        TextField nameField = new TextField("Name");

        ComboBox<CodeStore> countryCodes = new ComboBox<>("Country code");
        ComboBox.ItemFilter<CodeStore> countryCodeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupCountries();
        if(countries == null){
            countryCodes.setErrorMessage("Error happened while getting countries");
            countryCodes.setEnabled(false);
            countryCodes.setInvalid(true);
            saveButton.setEnabled(false);
        }
        else{
            countryCodes.setItems(countryCodeFilter, countries);
            countryCodes.setItemLabelGenerator(CodeStore::getName);
        }

        TextField zipCodeField = new TextField("Zip code");

        if (entity != null) {
            nameField.setValue(entity.getName());
            countryCodes.setValue(entity.getCountryCode());
            zipCodeField.setValue(entity.getZipCode());
        }

        fl.add(nameField, countryCodes, zipCodeField, saveButton);
        createDialog.add(fl);

        saveButton.addClickListener(event -> {
            City city = Objects.requireNonNullElseGet(entity, City::new);
            city.setName(nameField.getValue());
            city.setCountryCode(countryCodes.getValue());
            city.setZipCode(zipCodeField.getValue());
            city.setDeleted(0L);
            EmsResponse response = null;
            if(entity != null){
                response = cityApi.update(city);
            }
            else{
                response = cityApi.save(city);
            }
            switch (response.getCode()){
                case 200:
                    Notification.show("City " + (entity == null ? "saved: " : "updated: ") + city.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    setupCities();
                    updateGridItems();
                    break;
                default: {
                    Notification.show("City " + (entity == null ? "saving " : "modifying " ) + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    setupCities();
                    updateGridItems();
                    return;
                }

            }
            updateGridItems();


            nameField.clear();
            countryCodes.clear();
            zipCodeField.clear();
            createDialog.close();
        });
        return createDialog;
    }

    private void setupCountries() {
        EmsResponse response = codeStoreApi.getChildren(StaticDatas.COUNTRIES_CODESTORE_ID);
        switch (response.getCode()){
            case 200:
                countries = (List<CodeStore>) response.getResponseData();
                break;
            default:
                countries = null;
                logger.error("CodeStore getChildrenError [countries]. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    @NeedCleanCoding
public class CityVO extends BaseVO {
        private City original;
        private String countryCode;
        private String name;
        private String zipCode;

        private LinkedHashMap<String, String> extraData;

        public CityVO(City city) {
            super(city.id, city.getDeleted());
            this.original = city;
            this.id = city.getId();
            this.deleted = city.getDeleted();
            this.name = original.getName();
            this.countryCode = original.getCountryCode().getName();
            this.zipCode = original.getZipCode();
        }
    }
}

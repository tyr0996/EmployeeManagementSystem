package hu.martin.ems.vaadin.component.Address;

import com.google.gson.Gson;
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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.CodeStoreIds;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.IEmsFilterableGridPage;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.AddressApiClient;
import hu.martin.ems.vaadin.api.CityApiClient;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import hu.martin.ems.vaadin.core.IEmsOptionColumnBaseDialogCreationForm;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route(value = "address/list", layout = MainView.class)
@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@RolesAllowed("ROLE_AddressesMenuOpenPermission")
@NeedCleanCoding
public class AddressList extends EmsFilterableGridComponent implements Creatable<Address>, IEmsFilterableGridPage, IEmsOptionColumnBaseDialogCreationForm<Address, AddressList.AddressVO> {

    @Getter
    private final AddressApiClient apiClient = BeanProvider.getBean(AddressApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private final CityApiClient cityApi = BeanProvider.getBean(CityApiClient.class);
    private Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;

    @Getter
    private PaginatedGrid<AddressVO, String> grid;
    List<Address> addresses;
    List<AddressVO> addressVOS;

    Grid.Column<AddressVO> cityColumn;
    Grid.Column<AddressVO> countryCodeColumn;
    Grid.Column<AddressVO> houseNumberColumn;
    Grid.Column<AddressVO> streetNameColumn;
    Grid.Column<AddressVO> streetTypeColumn;
    Grid.Column<AddressVO> extraData;
    private TextFilteringHeaderCell cityFilter;
    private TextFilteringHeaderCell countryCodeFilter;
    private TextFilteringHeaderCell houseNumberFilter;
    private TextFilteringHeaderCell streetNameFilter;
    private TextFilteringHeaderCell streetTypeFilter;
    private Logger logger = LoggerFactory.getLogger(Address.class);
    List<City> cityList;
    List<CodeStore> streetTypeList;
    List<CodeStore> countryList;
    private Button saveButton;

    @Autowired
    public AddressList(PaginationSetting paginationSetting) {
        AddressVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(AddressVO.class);
        setEntities();

        List<AddressVO> data = addresses.stream().map(AddressVO::new).collect(Collectors.toList());
        this.grid.setItems(data);

        cityColumn = grid.addColumn(v -> v.city);
        countryCodeColumn = grid.addColumn(v -> v.countryCode);
        houseNumberColumn = grid.addColumn(v -> v.houseNumber);
        streetNameColumn = grid.addColumn(v -> v.streetName);
        streetTypeColumn = grid.addColumn(v -> v.streetType);

        grid.addClassName("styling");
        grid.setPartNameGenerator(addressVO -> addressVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        extraData = this.grid.addComponentColumn(address -> createOptionColumn("Address", address));

        setFilteringHeaderRow();

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            AddressVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    public void setEntities() {
        EmsResponse emsResponse = apiClient.findAll();
        switch (emsResponse.getCode()) {
            case 200:
                addresses = (List<Address>) emsResponse.getResponseData();
                break;
            default:
                addresses = new ArrayList<>();
                logger.error("Address findAllError. Code: {}, Description: {}", emsResponse.getCode(), emsResponse.getDescription());
                Notification.show("EmsError happened while getting addresses")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
        }
    }

    private Stream<AddressVO> getFilteredStream() {
        return addressVOS.stream().filter(addressVO ->
                filterField(cityFilter, addressVO.city) &&
                filterField(countryCodeFilter, addressVO.countryCode) &&
                filterField(houseNumberFilter, addressVO.houseNumber) &&
                filterField(streetTypeFilter, addressVO.streetType) &&
                filterField(streetNameFilter, addressVO.streetName) &&
                addressVO.filterExtraData()
        );
    }
    
    private void setFilteringHeaderRow() {
        cityFilter = new TextFilteringHeaderCell("Search city...", this);
        countryCodeFilter = new TextFilteringHeaderCell("Search country code...", this);
        streetNameFilter = new TextFilteringHeaderCell("Search street name...", this);
        streetTypeFilter = new TextFilteringHeaderCell("Search street type...", this);
        houseNumberFilter = new TextFilteringHeaderCell("Search street type...", this);

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if (extraDataFilter.getValue().isEmpty()) {
                AddressVO.extraDataFilterMap.clear();
            } else {
                AddressVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(cityColumn).setComponent(styleFilterField(cityFilter, "City"));
        filterRow.getCell(countryCodeColumn).setComponent(styleFilterField(countryCodeFilter, "Country code"));
        filterRow.getCell(houseNumberColumn).setComponent(styleFilterField(houseNumberFilter, "House number"));
        filterRow.getCell(streetNameColumn).setComponent(styleFilterField(streetNameFilter, "Street name"));
        filterRow.getCell(streetTypeColumn).setComponent(styleFilterField(streetTypeFilter, "Street type"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }


    public void updateGridItems() {
        EmsResponse response = apiClient.findAllWithDeleted();
        List<Address> addresses;
        switch (response.getCode()) {
            case 200:
                addresses = (List<Address>) response.getResponseData();
                break;
            default:
                Notification.show("Refresh grid failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                addresses = new ArrayList<>();
                break;
        }
        addressVOS = addresses.stream().map(AddressVO::new).collect(Collectors.toList());
        List<AddressVO> data = getFilteredStream().collect(Collectors.toList());
        this.grid.setItems(data);
    }

    private void appendCloseButton(Dialog d) {
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> d.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        d.getHeader().add(closeButton);
    }

    public Dialog getSaveOrUpdateDialog(AddressVO entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " address");
        saveButton = new Button("Save");
        FormLayout formLayout = new FormLayout();

        appendCloseButton(createDialog);

        ComboBox<CodeStore> countryCodes = createCountryCodesComboBox();
        TextField streetNameField = new TextField("Street name");
        ComboBox<City> cities = createCitiesComboBox();
        ComboBox<CodeStore> streetTypes = createStreetTypesComboBox();

        TextField houseNumberField = new TextField("House number");

        if (entity != null) {
            countryCodes.setValue(entity.original.getCountryCode());
            cities.setValue(entity.original.getCity());
            streetNameField.setValue(entity.original.getStreetName());
            streetTypes.setValue(entity.original.getStreetType());
            houseNumberField.setValue(entity.original.getHouseNumber());
        }

        saveButton.addClickListener(event -> {
            Address address = Optional.ofNullable(entity)
                    .map(e -> e.original)
                    .orElseGet(Address::new);
            address.setCountryCode(countryCodes.getValue());
            address.setCity(cities.getValue());
            address.setStreetName(streetNameField.getValue());
            address.setStreetType(streetTypes.getValue());
            address.setDeleted(0L);
            address.setHouseNumber(houseNumberField.getValue());
            EmsResponse response = null;
            if (entity != null) {
                response = apiClient.update(entity.original);
            } else {
                response = apiClient.save(address);
            }
            switch (response.getCode()) {
                case 200:
                    Notification.show("Address " + (entity == null ? "saved: " : "updated: ") + address.getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                default: {
                    Notification.show("Address " + (entity == null ? "saving " : "modifying ") + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    updateGridItems();
                    break;
                }
            }

            countryCodes.setValue(null);
            cities.setValue(null);
            streetNameField.clear();
            streetTypes.setValue(null);
            houseNumberField.clear();
            createDialog.close();
            updateGridItems();
        });
        formLayout.add(countryCodes, cities, streetNameField, streetTypes, houseNumberField, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    private ComboBox<CodeStore> createStreetTypesComboBox() {
        setupStreetTypes();
        ComboBox<CodeStore> streetTypes = new ComboBox<>("Street type");
        ComboBox.ItemFilter<CodeStore> streetTypeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if (streetTypeList == null) {
            streetTypes.setInvalid(true);
            streetTypes.setErrorMessage("EmsError happened while getting street types");
            streetTypes.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            streetTypes.setInvalid(false);
            streetTypes.setEnabled(true);
            streetTypes.setItems(streetTypeFilter, streetTypeList);
            streetTypes.setItemLabelGenerator(CodeStore::getName);
        }

        return streetTypes;
    }

    private ComboBox<City> createCitiesComboBox() {
        setupCities();
        ComboBox<City> cities = new ComboBox<>("City");
        ComboBox.ItemFilter<City> cityFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if (cityList == null) {
            cities.setInvalid(true);
            cities.setErrorMessage("EmsError happened while getting cities");
            cities.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            cities.setInvalid(false);
            cities.setEnabled(true);
            cities.setItems(cityFilter, cityList);
            cities.setItemLabelGenerator(City::getName);
        }
        return cities;
    }

    private ComboBox<CodeStore> createCountryCodesComboBox() {
        setupCountries();
        ComboBox<CodeStore> countryCodes = new ComboBox<>("Country code");
        ComboBox.ItemFilter<CodeStore> countryCodeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if (countryList == null) {
            countryCodes.setInvalid(true);
            countryCodes.setErrorMessage("EmsError happened while getting countries");
            countryCodes.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            countryCodes.setInvalid(false);
            countryCodes.setEnabled(true);
            countryCodes.setItems(countryCodeFilter, countryList);
            countryCodes.setItemLabelGenerator(CodeStore::getName);
        }
        return countryCodes;
    }

    private void setupCities() {
        EmsResponse response = cityApi.findAll();
        switch (response.getCode()) {
            case 200:
                cityList = (List<City>) response.getResponseData();
                break;
            default:
                cityList = null;
                logger.error("City findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private void setupCountries() {
        EmsResponse response = codeStoreApi.getChildren(CodeStoreIds.COUNTRIES_CODESTORE_ID);
        switch (response.getCode()) {
            case 200:
                countryList = (List<CodeStore>) response.getResponseData();
                break;
            default:
                countryList = null;
                logger.error("CodeStore getChildrenError [country]. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private void setupStreetTypes() {
        EmsResponse response = codeStoreApi.getChildren(CodeStoreIds.STREET_TYPES_CODESTORE_ID);
        switch (response.getCode()) {
            case 200:
                streetTypeList = (List<CodeStore>) response.getResponseData();
                break;
            default:
                streetTypeList = null;
                logger.error("CodeStore getChildrenError [street type]. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    @NeedCleanCoding
    public class AddressVO extends BaseVO<Address> {
        private String countryCode;
        private String city;
        private String streetType;
        private String streetName;
        private String houseNumber;

        public AddressVO(Address address) {
            super(address.id, address.getDeleted(), address);
            this.countryCode = original.getCountryCode().getName();
            this.city = original.getCity().getName();
            this.streetName = original.getStreetName();
            this.streetType = original.getStreetType().getName();
            this.houseNumber = address.getHouseNumber();
        }
    }
}

package hu.martin.ems.vaadin.component.Address;

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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.auth.SecurityService;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.AddressApiClient;
import hu.martin.ems.vaadin.api.CityApiClient;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import jakarta.annotation.security.RolesAllowed;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@Route(value = "address/list", layout = MainView.class)
@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
//@PreAuthorize("hasRole('AddressesMenuOpenPermission')")
@RolesAllowed("ROLE_AddressesMenuOpenPermission")
@NeedCleanCoding
public class AddressList extends VerticalLayout implements Creatable<Address> {

    private final AddressApiClient addressApi = BeanProvider.getBean(AddressApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private final CityApiClient cityApi = BeanProvider.getBean(CityApiClient.class);
    private Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;
    private PaginatedGrid<AddressVO, String> grid;
    private final PaginationSetting paginationSetting;
    List<Address> addresses;
    List<AddressVO> addressVOS;

    Grid.Column<AddressVO> cityColumn;
    Grid.Column<AddressVO> countryCodeColumn;
    Grid.Column<AddressVO> houseNumberColumn;
    Grid.Column<AddressVO> streetNameColumn;
    Grid.Column<AddressVO> streetTypeColumn;
    Grid.Column<AddressVO> extraData;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();

    private static String cityFilterText = "";
    private static String countryCodeFilterText = "";
    private static String houseNumberFilterText = "";
    private static String streetNameFilterText = "";
    private static String streetTypeFilterText = "";

    private Button createOrModifySaveButton = new Button("Save");

    private Logger logger = LoggerFactory.getLogger(Address.class);
    List<City> cityList;
    List<CodeStore> streetTypeList;
    List<CodeStore> countryList;

    private Button saveButton;
//    private MainView mainView;

    @Autowired
    public AddressList(PaginationSetting paginationSetting) {
//        this.mainView = mainView;
        System.out.println("Felhasználó: " + BeanProvider.getBean(SecurityService.class).getAuthenticatedUser().getUsername());
        this.paginationSetting = paginationSetting;

        AddressVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(AddressVO.class);
        setupAddresses();


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


        //region Options column
        extraData = this.grid.addComponentColumn(address -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog edit = getSaveOrUpdateDialog(address.original);
                edit.open();
            });

            restoreButton.addClickListener(event -> {
                addressApi.restore(address.original);
                Notification.show("Address restored: " + address.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.addressApi.delete(address.original);
                Notification.show("Address deleted: " + address.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.addressApi.permanentlyDelete(address.original.getId());
                Notification.show("Address permanently deleted: " + address.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (address.original.getDeleted() == 0) {
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
            AddressVO.showDeletedCheckboxFilter.replace("deleted", newValue);

            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    private void setupAddresses() {
        EmsResponse emsResponse = addressApi.findAll();
        switch (emsResponse.getCode()){
            case 200:
                addresses = (List<Address>) emsResponse.getResponseData();
                break;
            default:
                addresses = new ArrayList<>();
                logger.error("Address findAllError. Code: {}, Description: {}", emsResponse.getCode(), emsResponse.getDescription());
                Notification.show("Error happened while getting addresses")
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                break;
        }
    }

    private Stream<AddressVO> getFilteredStream() {
        return addressVOS.stream().filter(addressVO ->
                (cityFilterText.isEmpty() || addressVO.city.toLowerCase().contains(cityFilterText.toLowerCase())) &&
                        (countryCodeFilterText.isEmpty() || addressVO.countryCode.toLowerCase().contains(countryCodeFilterText.toLowerCase())) &&
                        (houseNumberFilterText.isEmpty() || addressVO.houseNumber.toLowerCase().contains(houseNumberFilterText.toLowerCase())) &&
                        (streetTypeFilterText.isEmpty() || addressVO.streetType.toLowerCase().contains(streetTypeFilterText.toLowerCase())) &&
                        (streetNameFilterText.isEmpty() || addressVO.streetName.toLowerCase().contains(streetNameFilterText.toLowerCase())) &&
                        addressVO.filterExtraData()
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
        TextField cityFilter = new TextField();
        cityFilter.setPlaceholder("Search city...");
        cityFilter.setClearButtonVisible(true);
        cityFilter.addValueChangeListener(event -> {
            cityFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField countryCodeFilter = new TextField();
        countryCodeFilter.setPlaceholder("Search country code...");
        countryCodeFilter.setClearButtonVisible(true);
        countryCodeFilter.addValueChangeListener(event -> {
            countryCodeFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField streetNameFilter = new TextField();
        streetNameFilter.setPlaceholder("Search street name...");
        streetNameFilter.setClearButtonVisible(true);
        streetNameFilter.addValueChangeListener(event -> {
            streetNameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField streetTypeFilter = new TextField();
        streetTypeFilter.setPlaceholder("Search street type...");
        streetTypeFilter.setClearButtonVisible(true);
        streetTypeFilter.addValueChangeListener(event -> {
            streetTypeFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField houseNumberFilter = new TextField();
        houseNumberFilter.setPlaceholder("Search street type...");
        houseNumberFilter.setClearButtonVisible(true);
        houseNumberFilter.addValueChangeListener(event -> {
            houseNumberFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                AddressVO.extraDataFilterMap.clear();
            }
            else{
                AddressVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(cityColumn).setComponent(filterField(cityFilter, "City"));
        filterRow.getCell(countryCodeColumn).setComponent(filterField(countryCodeFilter, "Country code"));
        filterRow.getCell(houseNumberColumn).setComponent(filterField(houseNumberFilter, "House number"));
        filterRow.getCell(streetNameColumn).setComponent(filterField(streetNameFilter, "Street name"));
        filterRow.getCell(streetTypeColumn).setComponent(filterField(streetTypeFilter, "Street type"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }


    private void updateGridItems() {
        EmsResponse response = addressApi.findAllWithDeleted();
        List<Address> addresses;
        switch (response.getCode()){
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

    public Dialog getSaveOrUpdateDialog(Address entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " address");
        saveButton = new Button("Save");
        FormLayout formLayout = new FormLayout();

        ComboBox<CodeStore> countryCodes = createCountryCodesComboBox();
        TextField streetNameField = new TextField("Street name");
        ComboBox<City> cities = createCitiesComboBox();
        ComboBox<CodeStore> streetTypes = createStreetTypesComboBox();

        TextField houseNumberField = new TextField("House number");

        if (entity != null) {
            countryCodes.setValue(entity.getCountryCode());
            cities.setValue(entity.getCity());
            streetNameField.setValue(entity.getStreetName());
            streetTypes.setValue(entity.getStreetType());
            houseNumberField.setValue(entity.getHouseNumber());
        }

        saveButton.addClickListener(event -> {
            Address address = Objects.requireNonNullElseGet(entity, Address::new);
            address.setCountryCode(countryCodes.getValue());
            address.setCity(cities.getValue());
            address.setStreetName(streetNameField.getValue());
            address.setStreetType(streetTypes.getValue());
            address.setDeleted(0L);
            address.setHouseNumber(houseNumberField.getValue());
            EmsResponse response = null;
            if(entity != null){
                response = addressApi.update(entity);
            }
            else{
                response = addressApi.save(address);
            }
            switch (response.getCode()){
                case 200:
                    Notification.show("Address " + (entity == null ? "saved: " : "updated: ") + address.getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                default: {
                    Notification.show("Address " + (entity == null ? "saving " : "modifying " ) + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
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
        if(streetTypeList == null){
            streetTypes.setInvalid(true);
            streetTypes.setErrorMessage("Error happened while getting street types");
            streetTypes.setEnabled(false);
            saveButton.setEnabled(false);
        }
        else {
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
        if(cityList == null){
            cities.setInvalid(true);
            cities.setErrorMessage("Error happened while getting cities");
            cities.setEnabled(false);
            saveButton.setEnabled(false);
        }
        else {
            cities.setInvalid(false);
            cities.setEnabled(true);
            cities.setItems(cityFilter, cityList);
            cities.setItemLabelGenerator(City::getName);
        }
        return cities;
    }

    private ComboBox<CodeStore> createCountryCodesComboBox(){
        setupCountries();
        ComboBox<CodeStore> countryCodes = new ComboBox<>("Country code");
        ComboBox.ItemFilter<CodeStore> countryCodeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if(countryList == null){
            countryCodes.setInvalid(true);
            countryCodes.setErrorMessage("Error happened while getting countries");
            countryCodes.setEnabled(false);
            saveButton.setEnabled(false);
        }
        else {
            countryCodes.setInvalid(false);
            countryCodes.setEnabled(true);
            countryCodes.setItems(countryCodeFilter, countryList);
            countryCodes.setItemLabelGenerator(CodeStore::getName);
        }
        return countryCodes;
    }

    private void setupCities() {
        EmsResponse response = cityApi.findAll();
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

    private void setupCountries() {
        EmsResponse response = codeStoreApi.getChildren(StaticDatas.COUNTRIES_CODESTORE_ID);
        switch (response.getCode()){
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
        EmsResponse response = codeStoreApi.getChildren(StaticDatas.STREET_TYPES_CODESTORE_ID);
        switch (response.getCode()){
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
public class AddressVO extends BaseVO {
        private Address original;
        private String countryCode;
        private String city;
        private String streetType;
        private String streetName;
        private String houseNumber;

        public AddressVO(Address address) {
            super(address.id, address.getDeleted());
            this.original = address;
            this.countryCode = original.getCountryCode().getName();
            this.city = original.getCity().getName();
            this.streetName = original.getStreetName();
            this.streetType = original.getStreetType().getName();
            this.houseNumber = address.getHouseNumber();
        }
    }
}

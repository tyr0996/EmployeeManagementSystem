package hu.martin.ems.vaadin.component.City;

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
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.CodeStoreIds;
import hu.martin.ems.core.config.IconProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CityApiClient;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
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

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "city/list", layout = MainView.class)
@RolesAllowed("ROLE_CityMenuOpenPermission")
@NeedCleanCoding
public class CityList extends EmsFilterableGridComponent implements Creatable<City> {

    private final CityApiClient cityApi = BeanProvider.getBean(CityApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private boolean showDeleted = false;
    @Getter
    private PaginatedGrid<CityVO, String> grid;
    private PaginationSetting paginationSetting;

    List<CityVO> cityVOS;
    List<City> cityList;
    List<CodeStore> countries;

    private TextFilteringHeaderCell countryCodeFilter;
    private TextFilteringHeaderCell nameFilter;
    private TextFilteringHeaderCell zipCodeFilter;
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


    private void createCityList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        CityVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(CityVO.class);
        setupCountries();
        setupCities();

        countryCodeColumn = grid.addColumn(v -> v.countryCode);
        nameColumn = grid.addColumn(v -> v.name);
        zipCodeColumn = grid.addColumn(v -> v.zipCode);

        grid.addClassName("styling");
        grid.setPartNameGenerator(cityVO -> cityVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        extraData = this.grid.addComponentColumn(city -> {
            Button editButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).EDIT_ICON));
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(BeanProvider.getBean(IconProvider.class).create(BeanProvider.getBean(IconProvider.class).PERMANENTLY_DELETE_ICON));
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
                switch (resp.getCode()) {
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
                switch (response.getCode()) {
                    case 200: {
                        Notification.show("City permanently deleted: " + city.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        setupCities();
                        updateGridItems();
                        break;
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


        setFilteringHeaderRow();
        updateGridItems();
        add(hl, grid);
    }

    private void setupCities() {
        EmsResponse response = cityApi.findAllWithDeleted();
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

    private Stream<CityVO> getFilteredStream() {
        return cityVOS.stream().filter(cityVO ->
                filterField(zipCodeFilter, cityVO.zipCode) &&
                filterField(countryCodeFilter, cityVO.countryCode) &&
                filterField(nameFilter, cityVO.name) &&
                cityVO.filterExtraData());
//                        (zipCodeFilter.isEmpty() || cityVO.zipCode.toLowerCase().contains(zipCodeFilter.getFilterText().toLowerCase())) &&
//                        (countryCodeFilter.isEmpty() || cityVO.countryCode.toLowerCase().contains(countryCodeFilter.getFilterText().toLowerCase())) &&
//                        (nameFilter.isEmpty() || cityVO.name.toLowerCase().contains(nameFilter.getFilterText().toLowerCase())) &&
//                        cityVO.filterExtraData()
//        );
    }

    private void setFilteringHeaderRow() {
        countryCodeFilter = new TextFilteringHeaderCell("Search country code...", this);
        nameFilter = new TextFilteringHeaderCell("Search name...", this);
        zipCodeFilter = new TextFilteringHeaderCell("Search zip code...", this);

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if (extraDataFilter.getValue().isEmpty()) {
                CityVO.extraDataFilterMap.clear();
            } else {
                CityVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(countryCodeColumn).setComponent(styleFilterField(countryCodeFilter, "Country code"));
        filterRow.getCell(nameColumn).setComponent(styleFilterField(nameFilter, "Name"));
        filterRow.getCell(zipCodeColumn).setComponent(styleFilterField(zipCodeFilter, "ZipCode"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }


    public void updateGridItems() {
        if (cityList != null) {
            cityVOS = cityList.stream().map(CityVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        } else {
            Notification.show("Getting cities failed").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void appendCloseButton(Dialog d) {
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> d.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        d.getHeader().add(closeButton);
    }

    public Dialog getSaveOrUpdateDialog(City entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " city");
        appendCloseButton(createDialog);

        Button saveButton = new Button("Save");
        FormLayout fl = new FormLayout();
        TextField nameField = new TextField("Name");

        ComboBox<CodeStore> countryCodes = new ComboBox<>("Country code");
        ComboBox.ItemFilter<CodeStore> countryCodeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        setupCountries();
        if (countries == null) {
            countryCodes.setErrorMessage("EmsError happened while getting countries");
            countryCodes.setEnabled(false);
            countryCodes.setInvalid(true);
            saveButton.setEnabled(false);
        } else {
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
            if (entity != null) {
                response = cityApi.update(city);
            } else {
                response = cityApi.save(city);
            }
            switch (response.getCode()) {
                case 200:
                    Notification.show("City " + (entity == null ? "saved: " : "updated: ") + city.getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    setupCities();
                    updateGridItems();
                    break;
                default: {
                    Notification.show("City " + (entity == null ? "saving " : "modifying ") + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
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
        EmsResponse response = codeStoreApi.getChildren(CodeStoreIds.COUNTRIES_CODESTORE_ID);
        switch (response.getCode()) {
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

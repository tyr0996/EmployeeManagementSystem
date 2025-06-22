package hu.martin.ems.vaadin.component.City;

import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
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
import hu.martin.ems.core.vaadin.ExtraDataFilterField;
import hu.martin.ems.core.vaadin.Switch;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CityApiClient;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import hu.martin.ems.vaadin.core.EmsComboBox;
import hu.martin.ems.vaadin.core.EmsDialog;
import hu.martin.ems.vaadin.core.IEmsOptionColumnBaseDialogCreationForm;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "city/list", layout = MainView.class)
@RolesAllowed("ROLE_CityMenuOpenPermission")
@NeedCleanCoding
public class CityList extends EmsFilterableGridComponent implements Creatable<City>, IEmsOptionColumnBaseDialogCreationForm<City, CityList.CityVO> {

    @Getter
    private final CityApiClient apiClient = BeanProvider.getBean(CityApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private boolean showDeleted = false;
    @Getter
    private PaginatedGrid<CityVO, String> grid;

    List<CityVO> cityVOS;
    List<City> cityList;
    List<CodeStore> countries;

    private TextFilteringHeaderCell countryCodeFilter;
    private TextFilteringHeaderCell nameFilter;
    private TextFilteringHeaderCell zipCodeFilter;
    private ExtraDataFilterField extraDataFilter;
    private Grid.Column<CityVO> extraData;

    private Grid.Column<CityVO> countryCodeColumn;
    private Grid.Column<CityVO> nameColumn;
    private Grid.Column<CityVO> zipCodeColumn;
    private Logger logger = LoggerFactory.getLogger(City.class);
    private Gson gson = BeanProvider.getBean(Gson.class);

    private LinkedHashMap<String, List<String>> showDeletedCheckboxFilter;

    @Autowired
    public CityList(PaginationSetting paginationSetting) {
        showDeletedCheckboxFilter = new LinkedHashMap<>();
        showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(CityVO.class);
        setupCountries();
        setEntities();

        countryCodeColumn = grid.addColumn(v -> v.countryCode);
        nameColumn = grid.addColumn(v -> v.name);
        zipCodeColumn = grid.addColumn(v -> v.zipCode);

        grid.addClassName("styling");
        grid.setPartNameGenerator(cityVO -> cityVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        extraData = this.grid.addComponentColumn(city -> createOptionColumn("City", city));
        //endregion

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Switch showDeletedSwitch = new Switch("Show deleted");
        showDeletedSwitch.addClickListener(event -> {
            showDeleted = event.getSource().getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            showDeletedCheckboxFilter.replace("deleted", newValue);

            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedSwitch, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedSwitch);
        hl.setAlignSelf(Alignment.CENTER, create);


        setFilteringHeaderRow();
        updateGridItems();
        add(hl, grid);
    }

    public void setEntities() {
        EmsResponse response = apiClient.findAllWithDeleted();
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
                filterExtraData(extraDataFilter, cityVO, showDeletedCheckboxFilter));
    }

    private void setFilteringHeaderRow() {
        countryCodeFilter = new TextFilteringHeaderCell("Search country code...", this);
        nameFilter = new TextFilteringHeaderCell("Search name...", this);
        zipCodeFilter = new TextFilteringHeaderCell("Search zip code...", this);

        extraDataFilter = new ExtraDataFilterField("", this);

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


    public EmsDialog getSaveOrUpdateDialog(CityVO entity) {
        EmsDialog createDialog = new EmsDialog((entity == null ? "Create" : "Modify") + " city");

        Button saveButton = new Button("Save");
        FormLayout fl = new FormLayout();
        TextField nameField = new TextField("Name");

        EmsComboBox<CodeStore> countryCodes = new EmsComboBox<>("Country code", this::setupCountries, saveButton, "EmsError happened while getting countries");

        TextField zipCodeField = new TextField("Zip code");

        if (entity != null) {
            nameField.setValue(entity.original.getName());
            countryCodes.setValue(entity.original.getCountryCode());
            zipCodeField.setValue(entity.original.getZipCode());
        }

        fl.add(nameField, countryCodes, zipCodeField, saveButton);
        createDialog.add(fl);

        saveButton.addClickListener(event -> {
            City city = Optional.ofNullable(entity)
                    .map(e -> e.original)
                    .orElseGet(City::new);
            city.setName(nameField.getValue());
            city.setCountryCode(countryCodes.getValue());
            city.setZipCode(zipCodeField.getValue());
            city.setDeleted(0L);
            EmsResponse response = null;
            if (entity != null) {
                response = apiClient.update(city);
            } else {
                response = apiClient.save(city);
            }
            switch (response.getCode()) {
                case 200:
                    Notification.show("City " + (entity == null ? "saved: " : "updated: ") + city.getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    setEntities();
                    updateGridItems();
                    break;
                default: {
                    Notification.show("City " + (entity == null ? "saving " : "modifying ") + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    setEntities();
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

    private List<CodeStore> setupCountries() {
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
        return countries;
    }

    @NeedCleanCoding
    public class CityVO extends BaseVO<City> {
        private String countryCode;
        private String name;
        private String zipCode;

        public CityVO(City city) {
            super(city.id, city.getDeleted(), city);
            this.original = city;
            this.id = city.getId();
            this.deleted = city.getDeleted();
            this.name = original.getName();
            this.countryCode = original.getCountryCode().getName();
            this.zipCode = original.getZipCode();
        }
    }
}

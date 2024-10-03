package hu.martin.ems.vaadin.component.City;

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.CityApiClient;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.Creatable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "city/list", layout = MainView.class)
@AnonymousAllowed
@NeedCleanCoding
public class CityList extends VerticalLayout implements Creatable<City> {

    private final CityApiClient cityApi = BeanProvider.getBean(CityApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private boolean showDeleted = false;
    private PaginatedGrid<CityVO, String> grid;
    private final PaginationSetting paginationSetting;

    List<CityVO> cityVOS;

    private String countryCodeFilterText = "";
    private String nameFilterText = "";
    private String zipCodeFilterText = "";

    private Grid.Column<CityVO> countryCodeColumn;
    private Grid.Column<CityVO> nameColumn;
    private Grid.Column<CityVO> zipCodeColumn;


    @Autowired
    public CityList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(CityVO.class);
        List<City> cities = cityApi.findAll();
        cityVOS = cities.stream().map(CityVO::new).collect(Collectors.toList());
        grid.setItems(getFilteredStream().toList());

        countryCodeColumn = grid.addColumn(v -> v.countryCode);
        nameColumn = grid.addColumn(v -> v.name);
        zipCodeColumn = grid.addColumn(v -> v.zipCode);

        grid.addClassName("styling");
        grid.setPartNameGenerator(cityVO -> cityVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        setFilteringHeaderRow();

        //region Options column
        Grid.Column<CityVO> options = this.grid.addComponentColumn(city -> {
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
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.cityApi.delete(city.original);
                Notification.show("City deleted: " + city.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.cityApi.permanentlyDelete(city.original.getId());
                Notification.show("City permanently deleted: " + city.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (city.original.getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (city.original.getDeleted() == 1) {
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
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    private Stream<CityVO> getFilteredStream() {

        return cityVOS.stream().filter(cityVO ->
                (zipCodeFilterText.isEmpty() || cityVO.zipCode.toLowerCase().contains(zipCodeFilterText.toLowerCase())) &&
                        (countryCodeFilterText.isEmpty() || cityVO.countryCode.toLowerCase().contains(countryCodeFilterText.toLowerCase())) &&
                        (nameFilterText.isEmpty() || cityVO.name.toLowerCase().contains(nameFilterText.toLowerCase())) &&
                        (showDeleted ? (cityVO.deleted == 0 || cityVO.deleted == 1) : cityVO.deleted == 0)
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

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();;
        filterRow.getCell(countryCodeColumn).setComponent(filterField(countryCodeFilter, "Country code"));
        filterRow.getCell(nameColumn).setComponent(filterField(nameFilter, "Name"));
        filterRow.getCell(zipCodeColumn).setComponent(filterField(zipCodeFilter, "ZipCode"));
    }


    private void updateGridItems() {
        List<City> cities = cityApi.findAllWithDeleted();
        cityVOS = cities.stream().map(CityVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        //this.grid.setItems(cities.stream().map(CityVO::new).collect(Collectors.toList()));
    }

    public Dialog getSaveOrUpdateDialog(City entity){
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " city");
        FormLayout fl = new FormLayout();
        TextField nameField = new TextField("Name");

        ComboBox<CodeStore> countryCodes = new ComboBox<>("Country code");
        ComboBox.ItemFilter<CodeStore> countryCodeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        countryCodes.setItems(countryCodeFilter, codeStoreApi.getChildren(StaticDatas.COUNTRIES_CODESTORE_ID));
        countryCodes.setItemLabelGenerator(CodeStore::getName);

        TextField zipCodeField = new TextField("Zip code");

        if (entity != null) {
            nameField.setValue(entity.getName());
            countryCodes.setValue(entity.getCountryCode());
            zipCodeField.setValue(entity.getZipCode());
        }

        Button saveButton = new Button("Save");
        fl.add(nameField, countryCodes, zipCodeField, saveButton);
        createDialog.add(fl);

        saveButton.addClickListener(event -> {
            City city = Objects.requireNonNullElseGet(entity, City::new);
            city.setName(nameField.getValue());
            city.setCountryCode(countryCodes.getValue());
            city.setZipCode(zipCodeField.getValue());
            city.setDeleted(0L);
            if(entity != null){
                cityApi.update(city);
            }
            else{
                cityApi.save(city);
            }
            updateGridItems();

            Notification.show("City " + (entity == null ? "saved: " : "updated: ") + city.getName())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            countryCodes.clear();
            zipCodeField.clear();
            createDialog.close();
        });
        return createDialog;
    }

    @NeedCleanCoding
public class CityVO {
        private City original;
        private Long deleted;
        private Long id;
        private String countryCode;
        private String name;
        private String zipCode;

        public CityVO(City city) {
            this.original = city;
            this.id = city.getId();
            this.deleted = city.getDeleted();
            this.name = original.getName();
            this.countryCode = original.getCountryCode().getName();
            this.zipCode = original.getZipCode();
        }
    }
}

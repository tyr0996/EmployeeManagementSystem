package hu.martin.ems.vaadin.component.City;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.service.CityService;
import hu.martin.ems.service.CodeStoreService;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.component.Creatable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@Route(value = "city/list", layout = MainView.class)
public class CityList extends VerticalLayout implements Creatable<City> {

    private final CityService cityService;
    private final CodeStoreService codeStoreService;
    private boolean showDeleted = false;
    private PaginatedGrid<CityVO, String> grid;
    private final PaginationSetting paginationSetting;


    @Autowired
    public CityList(CityService cityService,
                    CodeStoreService codeStoreService,
                    PaginationSetting paginationSetting) {
        this.cityService = cityService;
        this.codeStoreService = codeStoreService;
        this.paginationSetting = paginationSetting;


        this.grid = new PaginatedGrid<>(CityVO.class);
        List<City> citys = cityService.findAll(false);
        List<CityVO> data = citys.stream().map(CityVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("deleted");
        this.grid.removeColumnByKey("id");
        grid.addClassName("styling");
        grid.setPartNameGenerator(cityVO -> cityVO.getDeleted() != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        this.grid.addComponentColumn(city -> {
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
                this.cityService.restore(city.getOriginal());
                Notification.show("City restored: " + city.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.cityService.delete(city.getOriginal());
                Notification.show("City deleted: " + city.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.cityService.permanentlyDelete(city.getOriginal());
                Notification.show("City permanently deleted: " + city.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (city.getOriginal().getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (city.getOriginal().getDeleted() == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        }).setHeader("Options");

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

    private void updateGridItems() {
        List<City> citys = this.cityService.findAll(showDeleted);
        this.grid.setItems(citys.stream().map(CityVO::new).collect(Collectors.toList()));
    }

    @Override
    public Dialog getSaveOrUpdateDialog(City entity){
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " city");
        FormLayout fl = new FormLayout();
        TextField nameField = new TextField("Name");

        ComboBox<CodeStore> countryCodes = new ComboBox<>("Country code");
        ComboBox.ItemFilter<CodeStore> countryCodeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        countryCodes.setItems(countryCodeFilter, codeStoreService.getChildren(StaticDatas.COUNTRIES_CODESTORE_ID));
        countryCodes.setItemLabelGenerator(CodeStore::getName);

        TextField zipCodeField = new TextField("Zip code");

        Button saveButton = new Button("Save");
        fl.add(nameField, countryCodes, zipCodeField, saveButton);
        createDialog.add(fl);

        saveButton.addClickListener(event -> {
            City city = Objects.requireNonNullElseGet(entity, City::new);
            city.setName(nameField.getValue());
            city.setCountryCode(countryCodes.getValue());
            city.setZipCode(zipCodeField.getValue());
            city.setDeleted(0L);
            this.cityService.saveOrUpdate(city);

            Notification.show("City saved: " + city.getName())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            nameField.clear();
            countryCodes.clear();
            zipCodeField.clear();
            createDialog.close();
        });
        return createDialog;
    }

    @Getter
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

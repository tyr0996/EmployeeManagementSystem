package hu.martin.ems.vaadin.component.Address;

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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.AddressApiClient;
import hu.martin.ems.vaadin.api.CityApiClient;
import hu.martin.ems.vaadin.api.CodeStoreApiClient;
import hu.martin.ems.vaadin.component.Creatable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@Route(value = "address/list", layout = MainView.class)
@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@AnonymousAllowed
@NeedCleanCoding
public class AddressList extends VerticalLayout implements Creatable<Address> {

    private final AddressApiClient addressApi = BeanProvider.getBean(AddressApiClient.class);
    private final CodeStoreApiClient codeStoreApi = BeanProvider.getBean(CodeStoreApiClient.class);
    private final CityApiClient cityApi = BeanProvider.getBean(CityApiClient.class);
    private boolean showDeleted = false;
    private PaginatedGrid<AddressVO, String> grid;
    private final PaginationSetting paginationSetting;

    @Autowired
    public AddressList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(AddressVO.class);
        List<Address> addresses = addressApi.findAll();
        List<AddressVO> data = addresses.stream().map(AddressVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("id");
        this.grid.removeColumnByKey("deleted");
        grid.addClassName("styling");
        grid.setPartNameGenerator(addressVO -> addressVO.getDeleted() != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        this.grid.addComponentColumn(address -> {
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
                addressApi.restore(address.getOriginal());
                Notification.show("Address restored: " + address.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.addressApi.delete(address.getOriginal());
                Notification.show("Address deleted: " + address.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.addressApi.permanentlyDelete(address.getOriginal().getId());
                Notification.show("Address permanently deleted: " + address.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (address.getOriginal().getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (address.getOriginal().getDeleted() == 1) {
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
        List<Address> addresses = showDeleted ? addressApi.findAllWithDeleted() : addressApi.findAll();
        this.grid.setItems(addresses.stream().map(AddressVO::new).collect(Collectors.toList()));
    }

    public Dialog getSaveOrUpdateDialog(Address entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " address");
        FormLayout formLayout = new FormLayout();

        ComboBox<CodeStore> countryCodes = new ComboBox<>("Country code");
        ComboBox.ItemFilter<CodeStore> countryCodeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        countryCodes.setItems(countryCodeFilter, codeStoreApi.getChildren(StaticDatas.COUNTRIES_CODESTORE_ID));
        countryCodes.setItemLabelGenerator(CodeStore::getName);

        ComboBox<City> citys = new ComboBox<>("City");
        ComboBox.ItemFilter<City> cityFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        citys.setItems(cityFilter, cityApi.findAll());
        citys.setItemLabelGenerator(City::getName);

        TextField streetNameField = new TextField("Street name");

        ComboBox<CodeStore> streetTypes = new ComboBox<>("Street type");
        ComboBox.ItemFilter<CodeStore> streetTypeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        streetTypes.setItems(streetTypeFilter, codeStoreApi.getChildren(StaticDatas.STREET_TYPES_CODESTORE_ID));
        streetTypes.setItemLabelGenerator(CodeStore::getName);

        TextField houseNumberField = new TextField("House number");

        Button saveButton = new Button("Save");

        if (entity != null) {
            countryCodes.setValue(entity.getCountryCode());
            citys.setValue(entity.getCity());
            streetNameField.setValue(entity.getStreetName());
            streetTypes.setValue(entity.getStreetType());
            houseNumberField.setValue(entity.getHouseNumber());
        }

        saveButton.addClickListener(event -> {
            Address address = Objects.requireNonNullElseGet(entity, Address::new);
            address.setCountryCode(countryCodes.getValue());
            address.setCity(citys.getValue());
            address.setStreetName(streetNameField.getValue());
            address.setStreetType(streetTypes.getValue());
            address.setDeleted(0L);
            address.setHouseNumber(houseNumberField.getValue());
            if(entity != null){
                addressApi.update(entity);
            }
            else{
                addressApi.save(address);
            }

            Notification.show("Address saved: " + address.getName())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            countryCodes.clear();
            citys.clear();
            streetNameField.clear();
            streetTypes.clear();
            houseNumberField.clear();
            createDialog.close();
            updateGridItems();
        });
        formLayout.add(countryCodes, citys, streetNameField, streetTypes, houseNumberField, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    @Getter
    @NeedCleanCoding
public class AddressVO {
        private Address original;
        private Long id;
        private Long deleted;
        private String countryCode;
        private String city;
        private String streetType;
        private String streetName;
        private String houseNumber;

        public AddressVO(Address address) {
            this.original = address;
            this.id = address.getId();
            this.deleted = address.getDeleted();
            this.countryCode = original.getCountryCode().getName();
            this.city = original.getCity().getName();
            this.streetName = original.getStreetName();
            this.streetType = original.getStreetType().getName();
            this.houseNumber = address.getHouseNumber();
        }
    }
}

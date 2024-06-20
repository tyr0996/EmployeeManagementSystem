package hu.martin.ems.vaadin.component.Address;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.service.AddressService;
import hu.martin.ems.service.CityService;
import hu.martin.ems.service.CodeStoreService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "address/create", layout = MainView.class)
public class AddressCreate extends VerticalLayout {

    public static Address a;
    private final AddressService addressService;
    private final CodeStoreService codeStoreService;
    private final CityService cityService;

    @Autowired
    public AddressCreate(AddressService addressService,
                         CodeStoreService codeStoreService,
                         CityService cityService) {
        this.addressService = addressService;
        this.codeStoreService = codeStoreService;
        this.cityService = cityService;
        FormLayout formLayout = new FormLayout();

        ComboBox<CodeStore> countryCodes = new ComboBox<>("Country code");
        ComboBox.ItemFilter<CodeStore> countryCodeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        countryCodes.setItems(countryCodeFilter, codeStoreService.getChildren(StaticDatas.COUNTRIES_CODESTORE_ID));
        countryCodes.setItemLabelGenerator(CodeStore::getName);

        ComboBox<City> citys = new ComboBox<>("City");
        ComboBox.ItemFilter<City> cityFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        citys.setItems(cityFilter, cityService.findAll());
        citys.setItemLabelGenerator(City::getName);

        TextField streetNameField = new TextField("Street name");

        ComboBox<CodeStore> streetTypes = new ComboBox<>("Street type");
        ComboBox.ItemFilter<CodeStore> streetTypeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        streetTypes.setItems(streetTypeFilter, codeStoreService.getChildren(StaticDatas.STREET_TYPES_CODESTORE_ID));
        streetTypes.setItemLabelGenerator(CodeStore::getName);

        TextField houseNumberField = new TextField("House number");

        Button saveButton = new Button("Save");

        if (a != null) {
            countryCodes.setValue(a.getCountryCode());
            citys.setValue(a.getCity());
            streetNameField.setValue(a.getStreetName());
            streetTypes.setValue(a.getStreetType());
            houseNumberField.setValue(a.getHouseNumber());
        }

        saveButton.addClickListener(event -> {
            Address address = Objects.requireNonNullElseGet(a, Address::new);
            address.setCountryCode(countryCodes.getValue());
            address.setCity(citys.getValue());
            address.setStreetName(streetNameField.getValue());
            address.setStreetType(streetTypes.getValue());
            address.setDeleted(0L);
            address.setHouseNumber(houseNumberField.getValue());
            this.addressService.saveOrUpdate(address);

            Notification.show("Address saved: " + address.getName());
            countryCodes.clear();
            citys.clear();
            streetNameField.clear();
            streetTypes.clear();
            houseNumberField.clear();
        });

        formLayout.add(countryCodes, citys, streetNameField, streetTypes, houseNumberField, saveButton);
        add(formLayout);
    }
}

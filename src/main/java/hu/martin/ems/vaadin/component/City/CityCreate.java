package hu.martin.ems.vaadin.component.City;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.model.City;
import hu.martin.ems.model.CodeStore;
import hu.martin.ems.service.CityService;
import hu.martin.ems.service.CodeStoreService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "city/create", layout = MainView.class)
public class CityCreate extends VerticalLayout {

    public static City c;
    private final CityService cityService;
    private final CodeStoreService codeStoreService;

    @Autowired
    public CityCreate(CityService cityService,
                      CodeStoreService codeStoreService) {
        this.cityService = cityService;
        this.codeStoreService = codeStoreService;
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");

        ComboBox<CodeStore> countryCodes = new ComboBox<>("Country code");
        ComboBox.ItemFilter<CodeStore> countryCodeFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        countryCodes.setItems(countryCodeFilter, codeStoreService.getChildren(StaticDatas.COUNTRIES_CODESTORE_ID));
        countryCodes.setItemLabelGenerator(CodeStore::getName);

        TextField zipCodeField = new TextField("Zip code");

        Button saveButton = new Button("Save");

        if (c != null) {
            nameField.setValue(c.getName());
            countryCodes.setValue(c.getCountryCode());
            zipCodeField.setValue(c.getZipCode());
        }

        saveButton.addClickListener(event -> {
            City city = Objects.requireNonNullElseGet(c, City::new);
            city.setName(nameField.getValue());
            city.setCountryCode(countryCodes.getValue());
            city.setZipCode(zipCodeField.getValue());
            city.setDeleted(0L);
            this.cityService.saveOrUpdate(city);

            Notification.show("City saved: " + city.getName());
            nameField.clear();
            countryCodes.clear();
            zipCodeField.clear();
        });

        formLayout.add(nameField, countryCodes, zipCodeField, saveButton);
        add(formLayout);
    }
}

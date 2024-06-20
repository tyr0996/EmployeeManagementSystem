package hu.martin.ems.vaadin.component.Customer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.Customer;
import hu.martin.ems.service.AddressService;
import hu.martin.ems.service.CustomerService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "customer/create", layout = MainView.class)
public class CustomerCreate extends VerticalLayout {

    public static Customer c;
    private final CustomerService customerService;
    private final AddressService addressService;

    @Autowired
    public CustomerCreate(CustomerService customerService,
                          AddressService addressService) {
        this.customerService = customerService;
        this.addressService = addressService;
        FormLayout formLayout = new FormLayout();

        TextField firstNameField = new TextField("First name");

        TextField lastNameField = new TextField("Last name");

        ComboBox<Address> addresss = new ComboBox<>("Address");
        ComboBox.ItemFilter<Address> addressFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        addresss.setItems(addressFilter, addressService.findAll(false));
        addresss.setItemLabelGenerator(Address::getName);

        Button saveButton = new Button("Save");

        if (c != null) {
            firstNameField.setValue(c.getFirstName());
            lastNameField.setValue(c.getLastName());
            addresss.setValue(c.getAddress());
        }

        saveButton.addClickListener(event -> {
            Customer customer = Objects.requireNonNullElseGet(c, Customer::new);
            customer.setFirstName(firstNameField.getValue());
            customer.setLastName(lastNameField.getValue());
            customer.setAddress(addresss.getValue());
            customer.setDeleted(0L);
            this.customerService.saveOrUpdate(customer);

            Notification.show("Customer saved: " + customer);
            firstNameField.clear();
            lastNameField.clear();
            addresss.clear();
        });

        formLayout.add(firstNameField, lastNameField, addresss, saveButton);
        add(formLayout);
    }
}

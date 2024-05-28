package hu.martin.ems.vaadin.component.Supplier;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.service.SupplierService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "supplier/create", layout = MainView.class)
public class SupplierCreate extends VerticalLayout {

    public static Supplier s;
    private final SupplierService supplierService;

    @Autowired
    public SupplierCreate(SupplierService supplierService) {
        this.supplierService = supplierService;
        FormLayout formLayout = new FormLayout();

        TextField nameField = new TextField("Name");

        ComboBox<Address> addresss = new ComboBox<>("Address");
        ComboBox.ItemFilter<Address> addressFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        addresss.setItems(addressFilter);
        addresss.setItemLabelGenerator(Address::getName);

        Button saveButton = new Button("Save");

        if (s != null) {
            nameField.setValue(s.getName());
            addresss.setValue(s.getAddress());
        }

        saveButton.addClickListener(event -> {
            Supplier supplier = Objects.requireNonNullElseGet(s, Supplier::new);
            supplier.setName(nameField.getValue());
            supplier.setAddress(addresss.getValue());
            supplier.setDeleted(0L);
            this.supplierService.saveOrUpdate(supplier);

            Notification.show("Supplier saved: " + supplier);
            nameField.clear();
            addresss.clear();
        });

        formLayout.add(nameField, addresss, saveButton);
        add(formLayout);
    }
}

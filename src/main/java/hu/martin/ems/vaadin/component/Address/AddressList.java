package hu.martin.ems.vaadin.component.Address;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Address;
import hu.martin.ems.service.AddressService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "address/list", layout = MainView.class)
public class AddressList extends VerticalLayout {

    private final AddressService addressService;
    private boolean showDeleted = false;
    private Grid<AddressVO> grid;

    @Autowired
    public AddressList(AddressService addressService) {
        this.addressService = addressService;

        this.grid = new Grid<>(AddressVO.class);
        List<Address> addresss = addressService.findAll(false);
        List<AddressVO> data = addresss.stream().map(AddressVO::new).toList();
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");

        //region Options column
        this.grid.addComponentColumn(address -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");

            editButton.addClickListener(event -> {
                AddressCreate.a = address.getOriginal();
                UI.getCurrent().navigate("address/create");
            });

            restoreButton.addClickListener(event -> {
                this.addressService.restore(address.getOriginal());
                Notification.show("Address restored: " + address.getOriginal().getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.addressService.delete(address.getOriginal());
                Notification.show("Address deleted: " + address.getOriginal().getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.addressService.permanentlyDelete(address.getOriginal());
                Notification.show("Address permanently deleted: " + address.getOriginal().getName());
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if(address.getOriginal().getDeleted() == 0){ actions.add(editButton, deleteButton); }
            else if(address.getOriginal().getDeleted() == 1){ actions.add(permanentDeleteButton, restoreButton); }
            return actions;
        }).setHeader("Options");

        //endregion

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            updateGridItems();
        });

        add(showDeletedCheckbox, grid);
    }

    private void updateGridItems() {
        List<Address> addresss = this.addressService.findAll(showDeleted);
        this.grid.setItems(addresss.stream().map(AddressVO::new).toList());
    }

    @Getter
    public class AddressVO {
        private Address original;
        private Long id;
        private String countryCode;
        private String city;
        private String streetType;
        private String streetName;
        private String houseNumber;

        public AddressVO(Address address) {
            this.original = address;
            this.id = address.getId();
            this.countryCode = original.getCountryCode().getName();
            this.city = original.getCity().getName();
            this.streetName = original.getStreetName();
            this.streetType = original.getStreetType().getName();
            this.houseNumber = address.getHouseNumber();
        }
    }
}

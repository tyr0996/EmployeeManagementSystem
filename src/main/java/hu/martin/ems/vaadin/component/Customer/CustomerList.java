package hu.martin.ems.vaadin.component.Customer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Customer;
import hu.martin.ems.service.CustomerService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "customer/list", layout = MainView.class)
public class CustomerList extends VerticalLayout {

    private final CustomerService customerService;
    private boolean showDeleted = false;
    private Grid<CustomerVO> grid;

    @Autowired
    public CustomerList(CustomerService customerService) {
        this.customerService = customerService;

        this.grid = new Grid<>(CustomerVO.class);
        List<Customer> customers = customerService.findAll(false);
        List<CustomerVO> data = customers.stream().map(CustomerVO::new).toList();
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");

        //region Options column
        this.grid.addComponentColumn(customer -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");

            editButton.addClickListener(event -> {
                CustomerCreate.c = customer.getOriginal();
                UI.getCurrent().navigate("customer/create");
            });

            restoreButton.addClickListener(event -> {
                this.customerService.restore(customer.getOriginal());
                Notification.show("Customer restored: " + customer.getOriginal().getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.customerService.delete(customer.getOriginal());
                Notification.show("Customer deleted: " + customer.getOriginal().getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.customerService.permanentlyDelete(customer.getOriginal());
                Notification.show("Customer permanently deleted: " + customer.getOriginal().getName());
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if(customer.getOriginal().getDeleted() == 0){ actions.add(editButton, deleteButton); }
            else if(customer.getOriginal().getDeleted() == 1){ actions.add(permanentDeleteButton, restoreButton); }
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
        List<Customer> customers = this.customerService.findAll(showDeleted);
        this.grid.setItems(customers.stream().map(CustomerVO::new).toList());
    }

    @Getter
    public class CustomerVO {
        private Customer original;
        private Long deleted;
        private Long id;
        private String address;
        private String firstName;
        private String lastName;

        public CustomerVO(Customer customer) {
            this.original = customer;
            this.id = customer.getId();
            this.deleted = customer.getDeleted();
            this.firstName = original.getFirstName();
            this.lastName = original.getLastName();
            this.address = original.getAddress().getName();
        }
    }
}

package hu.martin.ems.vaadin.component.Customer;

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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.Customer;
import hu.martin.ems.service.AddressService;
import hu.martin.ems.service.CustomerService;
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
@Route(value = "customer/list", layout = MainView.class)
public class CustomerList extends VerticalLayout implements Creatable<Customer> {

    private final CustomerService customerService;
    private final AddressService addressService;
    private boolean showDeleted = false;
    private PaginatedGrid<CustomerVO, String> grid;
    private final PaginationSetting paginationSetting;

    @Autowired
    public CustomerList(CustomerService customerService,
                        AddressService addressService,
                        PaginationSetting paginationSetting) {
        this.customerService = customerService;
        this.addressService = addressService;
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(CustomerVO.class);
        List<Customer> customers = customerService.findAll(false);
        List<CustomerVO> data = customers.stream().map(CustomerVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        this.grid.removeColumnByKey("id");
        this.grid.removeColumnByKey("deleted");
        grid.addClassName("styling");
        grid.setPartNameGenerator(customerVO -> customerVO.getDeleted() != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        this.grid.addComponentColumn(customer -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(customer.original);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                this.customerService.restore(customer.getOriginal());
                Notification.show("Customer restored: " + customer.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.customerService.delete(customer.getOriginal());
                Notification.show("Customer deleted: " + customer.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.customerService.permanentlyDelete(customer.getOriginal());
                Notification.show("Customer permanently deleted: " + customer.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (customer.getOriginal().getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (customer.getOriginal().getDeleted() == 1) {
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
        List<Customer> customers = this.customerService.findAll(showDeleted);
        this.grid.setItems(customers.stream().map(CustomerVO::new).collect(Collectors.toList()));
    }

    @Override
    public Dialog getSaveOrUpdateDialog(Customer entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " customer");
        FormLayout formLayout = new FormLayout();

        TextField firstNameField = new TextField("First name");

        TextField lastNameField = new TextField("Last name");

        ComboBox<Address> addresss = new ComboBox<>("Address");
        ComboBox.ItemFilter<Address> addressFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        addresss.setItems(addressFilter, addressService.findAll(false));
        addresss.setItemLabelGenerator(Address::getName);

        EmailField emailField = new EmailField();
        emailField.setLabel("Email address");
        emailField.setErrorMessage("Enter a valid email address");
        emailField.setClearButtonVisible(true);

        Button saveButton = new Button("Save");

        if (entity != null) {
            firstNameField.setValue(entity.getFirstName());
            lastNameField.setValue(entity.getLastName());
            addresss.setValue(entity.getAddress());
            emailField.setValue(entity.getEmailAddress());
        }

        saveButton.addClickListener(event -> {
            Customer customer = Objects.requireNonNullElseGet(entity, Customer::new);
            customer.setFirstName(firstNameField.getValue());
            customer.setLastName(lastNameField.getValue());
            customer.setAddress(addresss.getValue());
            customer.setEmailAddress(emailField.getValue());
            customer.setDeleted(0L);
            this.customerService.saveOrUpdate(customer);

            Notification.show("Customer saved: " + customer)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            firstNameField.clear();
            lastNameField.clear();
            addresss.clear();
            emailField.clear();
            createDialog.close();
        });

        formLayout.add(firstNameField, lastNameField, addresss, emailField, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    @Getter
    public class CustomerVO {
        private Customer original;
        private Long deleted;
        private Long id;
        private String address;
        private String firstName;
        private String lastName;
        private String email;

        public CustomerVO(Customer customer) {
            this.original = customer;
            this.id = customer.getId();
            this.deleted = customer.getDeleted();
            this.firstName = original.getFirstName();
            this.lastName = original.getLastName();
            this.address = original.getAddress().getName();
            this.email = original.getEmailAddress();
        }
    }
}

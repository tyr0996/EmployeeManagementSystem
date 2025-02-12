package hu.martin.ems.vaadin.component.Customer;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.Customer;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.AddressApiClient;
import hu.martin.ems.vaadin.api.CustomerApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import jakarta.annotation.security.RolesAllowed;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
//@AnonymousAllowed
@RolesAllowed("ROLE_CustomerMenuOpenPermission")
@Route(value = "customer/list", layout = MainView.class)
@NeedCleanCoding
public class CustomerList extends VerticalLayout implements Creatable<Customer> {
    private boolean showDeleted = false;
    private CustomerApiClient customerApi = BeanProvider.getBean(CustomerApiClient.class);
    private AddressApiClient addressApi = BeanProvider.getBean(AddressApiClient.class);
    private PaginatedGrid<CustomerVO, String> grid;
    private final PaginationSetting paginationSetting;
    List<Customer> customers;
    List<CustomerVO> customerVOS;

    Grid.Column<CustomerVO> addressColumn;
    Grid.Column<CustomerVO> emailColumn;
    Grid.Column<CustomerVO> firstNameColumn;
    Grid.Column<CustomerVO> lastNameColumn;
    private final Gson gson = BeanProvider.getBean(Gson.class);

    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<CustomerVO> extraData;

    private String addressFilterText = "";
    private String emailFilterText = "";
    private String firstNameFilterText = "";
    private String lastNameFilterText = "";
    private Logger logger = LoggerFactory.getLogger(Customer.class);

    List<Address> addressList;
    private MainView mainView;

    @Autowired
    public CustomerList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;
        CustomerVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(CustomerVO.class);
//        setupCustomers();

//        List<CustomerVO> data = customers.stream().map(CustomerVO::new).collect(Collectors.toList());

        addressColumn = this.grid.addColumn(v -> v.address);
        emailColumn = this.grid.addColumn(v -> v.email);
        firstNameColumn = this.grid.addColumn(v -> v.firstName);
        lastNameColumn = this.grid.addColumn(v -> v.lastName);

//        this.grid.setItems(data);

        grid.addClassName("styling");
        grid.setPartNameGenerator(customerVO -> customerVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        extraData = this.grid.addComponentColumn(customerVO -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(customerVO.original);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                this.customerApi.restore(customerVO.original);
                Notification.show("Customer restored: " + customerVO.name)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.customerApi.delete(customerVO.original);
                switch (resp.getCode()){
                    case 200: {
                        Notification.show("Customer deleted: " + customerVO.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setupCustomers();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.customerApi.permanentlyDelete(customerVO.id);
                Notification.show("Customer permanently deleted: " + customerVO.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (customerVO.deleted == 0) {
                actions.add(editButton, deleteButton);
            }
            else{
                //} else if (customerVO.deleted == 1) {
                actions.add(permanentDeleteButton, restoreButton);
            }
            return actions;
        });


        setFilteringHeaderRow();
        //endregion

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Checkbox showDeletedCheckbox = new Checkbox("Show deleted");
        showDeletedCheckbox.addValueChangeListener(event -> {
            showDeleted = event.getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            CustomerVO.showDeletedCheckboxFilter.replace("deleted", newValue);

//            setupCustomers();
//            customerVOS = customers.stream().map(CustomerVO::new).collect(Collectors.toList());
            if(customers == null){
                customers = new ArrayList<>();
            }
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);
        updateGridItems();
        add(hl, grid);
    }

    private void setupCustomers() {
        EmsResponse response = customerApi.findAllWithDeleted();
        switch (response.getCode()){
            case 200:
                customers = (List<Customer>) response.getResponseData();
                break;
            default:
                customers = null;
                logger.error("Customer findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private Stream<CustomerVO> getFilteredStream() {

        return customerVOS.stream().filter(customerVO ->
                (addressFilterText.isEmpty() || customerVO.address.toLowerCase().contains(addressFilterText.toLowerCase())) &&
                (emailFilterText.isEmpty() || customerVO.email.toLowerCase().contains(emailFilterText.toLowerCase())) &&
                (firstNameFilterText.isEmpty() || customerVO.firstName.toLowerCase().contains(firstNameFilterText.toLowerCase())) &&
                (lastNameFilterText.isEmpty() || customerVO.lastName.toLowerCase().contains(lastNameFilterText.toLowerCase())) &&
                customerVO.filterExtraData()
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
        TextField addressFilter = new TextField();
        addressFilter.setPlaceholder("Search address...");
        addressFilter.setClearButtonVisible(true);
        addressFilter.addValueChangeListener(event -> {
            addressFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField emailFilter = new TextField();
        emailFilter.setPlaceholder("Search email...");
        emailFilter.setClearButtonVisible(true);
        emailFilter.addValueChangeListener(event -> {
            emailFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField firstNameFilter = new TextField();
        firstNameFilter.setPlaceholder("Search first name...");
        firstNameFilter.setClearButtonVisible(true);
        firstNameFilter.addValueChangeListener(event -> {
            firstNameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField lastNameFilter = new TextField();
        lastNameFilter.setPlaceholder("Search last name...");
        lastNameFilter.setClearButtonVisible(true);
        lastNameFilter.addValueChangeListener(event -> {
            lastNameFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                CustomerVO.extraDataFilterMap.clear();
            }
            else{
                CustomerVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();;
        filterRow.getCell(addressColumn).setComponent(filterField(addressFilter, "Address"));
        filterRow.getCell(emailColumn).setComponent(filterField(emailFilter, "Email"));
        filterRow.getCell(firstNameColumn).setComponent(filterField(firstNameFilter, "First name"));
        filterRow.getCell(lastNameColumn).setComponent(filterField(lastNameFilter, "Last name"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }

    private void updateGridItems() {
        customers = new ArrayList<>();
        setupCustomers();
        if(customers != null){
            customerVOS = customers.stream().map(CustomerVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        }
        else{
            Notification.show("Error happened while getting customers").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public Dialog getSaveOrUpdateDialog(Customer entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " customer");
        FormLayout formLayout = new FormLayout();
        Button saveButton = new Button("Save");

        TextField firstNameField = new TextField("First name");

        TextField lastNameField = new TextField("Last name");

        setupAddresses();

        ComboBox<Address> addresses = new ComboBox<>("Address");
        ComboBox.ItemFilter<Address> addressFilter = (element, filterString) ->
                element.getName().toLowerCase().contains(filterString.toLowerCase());
        if(addressList != null){
            addresses.setItems(addressFilter, addressList);
            addresses.setItemLabelGenerator(Address::getName);
        }
        else{
            addresses.setEnabled(false);
            addresses.setInvalid(true);
            addresses.setErrorMessage("Error happened while getting addresses");
            saveButton.setEnabled(false);
        }


        EmailField emailField = new EmailField();
        emailField.setLabel("Email address");
        emailField.setManualValidation(true);
        emailField.setErrorMessage("Enter a valid email address");
        emailField.setClearButtonVisible(true);



        if (entity != null) {
            firstNameField.setValue(entity.getFirstName());
            lastNameField.setValue(entity.getLastName());
            addresses.setValue(entity.getAddress());
            emailField.setValue(entity.getEmailAddress());
        }

        saveButton.addClickListener(event -> {
            Customer customer = Objects.requireNonNullElseGet(entity, Customer::new);
            customer.setFirstName(firstNameField.getValue());
            customer.setLastName(lastNameField.getValue());
            customer.setAddress(addresses.getValue());
            customer.setEmailAddress(emailField.getValue());
            customer.setDeleted(0L);
            EmsResponse response;
            if(entity != null){
                response = customerApi.update(customer);
            }
            else{
                response = customerApi.save(customer);
            }
            switch (response.getCode()){
                case 200: {
                    Notification.show("Customer " + (entity == null ? "saved: " : "updated: ") + ((Customer) response.getResponseData()).getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                }
                default: {
                    Notification.show("Customer " + (entity == null ? "saving " : "modifying " ) + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    updateGridItems();
                    return;
                }
            }

            firstNameField.clear();
            lastNameField.clear();
            addresses.clear();
            emailField.clear();
            createDialog.close();
            updateGridItems();
        });

        formLayout.add(firstNameField, lastNameField, addresses, emailField, saveButton);
        createDialog.add(formLayout);
        return createDialog;
    }

    private void setupAddresses() {
        EmsResponse emsResponse = addressApi.findAll();
        switch (emsResponse.getCode()){
            case 200:
                addressList = (List<Address>) emsResponse.getResponseData();
                break;
            default:
                addressList = null;
                logger.error("Address findAllError. Code: {}, Description: {}", emsResponse.getCode(), emsResponse.getDescription());
                break;
        }
    }

    @NeedCleanCoding
    public class CustomerVO extends BaseVO {
        private Customer original;
        private String address;
        private String firstName;
        private String lastName;
        private String email;
        private String name;

        public CustomerVO(Customer customer) {
            super(customer.getId(), customer.getDeleted());
            this.original = customer;
            this.firstName = original.getFirstName();
            this.lastName = original.getLastName();
            this.address = original.getAddress().getName();
            this.email = original.getEmailAddress();
            this.name = original.getName();
        }
    }
}

package hu.martin.ems.vaadin.component.Customer;

import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.ExtraDataFilterField;
import hu.martin.ems.core.vaadin.Switch;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Address;
import hu.martin.ems.model.Customer;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.AddressApiClient;
import hu.martin.ems.vaadin.api.CustomerApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import hu.martin.ems.vaadin.core.EmsComboBox;
import hu.martin.ems.vaadin.core.EmsDialog;
import hu.martin.ems.vaadin.core.IEmsOptionColumnBaseDialogCreationForm;
import jakarta.annotation.security.RolesAllowed;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@RolesAllowed("ROLE_CustomerMenuOpenPermission")
@Route(value = "customer/list", layout = MainView.class)
@NeedCleanCoding
public class CustomerList extends EmsFilterableGridComponent implements Creatable<Customer>, IEmsOptionColumnBaseDialogCreationForm<Customer, CustomerList.CustomerVO> {
    private boolean showDeleted = false;
    @Getter
    private final CustomerApiClient apiClient = BeanProvider.getBean(CustomerApiClient.class);
    private final AddressApiClient addressApi = BeanProvider.getBean(AddressApiClient.class);
    @Getter
    private PaginatedGrid<CustomerVO, String> grid;
    List<Customer> customers;
    List<CustomerVO> customerVOS;

    Grid.Column<CustomerVO> addressColumn;
    Grid.Column<CustomerVO> emailColumn;
    Grid.Column<CustomerVO> firstNameColumn;
    Grid.Column<CustomerVO> lastNameColumn;
    private final Gson gson = BeanProvider.getBean(Gson.class);

    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<CustomerVO> extraData;

    private TextFilteringHeaderCell addressFilter;
    private TextFilteringHeaderCell emailFilter;
    private TextFilteringHeaderCell firstNameFilter;
    private TextFilteringHeaderCell lastNameFilter;
    private ExtraDataFilterField extraDataFilter;
    private Logger logger = LoggerFactory.getLogger(Customer.class);

    List<Address> addressList;

    LinkedHashMap<String, List<String>> showDeletedCheckboxFilter;

    @Autowired
    public CustomerList(PaginationSetting paginationSetting) {
        showDeletedCheckboxFilter = new LinkedHashMap<>();
        showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(CustomerVO.class);

        addressColumn = this.grid.addColumn(v -> v.address);
        emailColumn = this.grid.addColumn(v -> v.email);
        firstNameColumn = this.grid.addColumn(v -> v.firstName);
        lastNameColumn = this.grid.addColumn(v -> v.lastName);

        grid.addClassName("styling");
        grid.setPartNameGenerator(customerVO -> customerVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        extraData = this.grid.addComponentColumn(customerVO -> createOptionColumn("Customer", customerVO));


        setFilteringHeaderRow();
        //endregion

        Button create = new Button("Create");
        create.addClickListener(event -> {
            Dialog d = getSaveOrUpdateDialog(null);
            d.open();
        });

        Switch showDeletedSwitch = new Switch("Show deleted");
        showDeletedSwitch.addClickListener(event -> {
            showDeleted = event.getSource().getValue();
            List<String> newValue = showDeleted ? Arrays.asList("1", "0") : Arrays.asList("0");
            showDeletedCheckboxFilter.replace("deleted", newValue);

            if (customers == null) {
                customers = new ArrayList<>();
            }
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedSwitch, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedSwitch);
        hl.setAlignSelf(Alignment.CENTER, create);
        updateGridItems();
        add(hl, grid);
    }

    public void setEntities() {
        EmsResponse response = apiClient.findAllWithDeleted();
        switch (response.getCode()) {
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
                filterField(addressFilter, customerVO.address) &&
                filterField(emailFilter, customerVO.email) &&
                filterField(firstNameFilter, customerVO.firstName) &&
                filterField(lastNameFilter, customerVO.lastName) &&
                filterExtraData(extraDataFilter, customerVO, showDeletedCheckboxFilter));
    }

    private void setFilteringHeaderRow() {
        addressFilter = new TextFilteringHeaderCell("Search address...", this);
        emailFilter = new TextFilteringHeaderCell("Search email...", this);
        firstNameFilter = new TextFilteringHeaderCell("Search first name...", this);
        lastNameFilter = new TextFilteringHeaderCell("Search last name...", this);
        extraDataFilter = new ExtraDataFilterField("", this);

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(addressColumn).setComponent(styleFilterField(addressFilter, "Address"));
        filterRow.getCell(emailColumn).setComponent(styleFilterField(emailFilter, "Email"));
        filterRow.getCell(firstNameColumn).setComponent(styleFilterField(firstNameFilter, "First name"));
        filterRow.getCell(lastNameColumn).setComponent(styleFilterField(lastNameFilter, "Last name"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }

    public void updateGridItems() {
        customers = new ArrayList<>();
        setEntities();
        if (customers != null) {
            customerVOS = customers.stream().map(CustomerVO::new).collect(Collectors.toList());
            this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
        } else {
            Notification.show("EmsError happened while getting customers").addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public EmsDialog getSaveOrUpdateDialog(CustomerVO entity) {
        EmsDialog createDialog = new EmsDialog((entity == null ? "Create" : "Modify") + " customer");

        FormLayout formLayout = new FormLayout();
        Button saveButton = new Button("Save");

        TextField firstNameField = new TextField("First name");

        TextField lastNameField = new TextField("Last name");

        EmsComboBox<Address> addresses = new EmsComboBox<>("Address", this::setupAddresses, saveButton, "EmsError happened while getting addresses");

        EmailField emailField = new EmailField();
        emailField.setLabel("Email address");
        emailField.setManualValidation(true);
        emailField.setErrorMessage("Enter a valid email address");
        emailField.setClearButtonVisible(true);


        if (entity != null) {
            firstNameField.setValue(entity.original.getFirstName());
            lastNameField.setValue(entity.original.getLastName());
            addresses.setValue(entity.original.getAddress());
            emailField.setValue(entity.original.getEmailAddress());
        }

        saveButton.addClickListener(event -> {
            Customer customer = Optional.ofNullable(entity)
                    .map(e -> e.original)
                    .orElseGet(Customer::new);
            customer.setFirstName(firstNameField.getValue());
            customer.setLastName(lastNameField.getValue());
            customer.setAddress(addresses.getValue());
            customer.setEmailAddress(emailField.getValue());
            customer.setDeleted(0L);
            EmsResponse response;
            if (entity != null) {
                response = apiClient.update(customer);
            } else {
                response = apiClient.save(customer);
            }
            switch (response.getCode()) {
                case 200: {
                    Notification.show("Customer " + (entity == null ? "saved: " : "updated: ") + ((Customer) response.getResponseData()).getName())
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                }
                default: {
                    Notification.show("Customer " + (entity == null ? "saving " : "modifying ") + "failed: " + response.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
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

    private List<Address> setupAddresses() {
        EmsResponse emsResponse = addressApi.findAll();
        switch (emsResponse.getCode()) {
            case 200:
                addressList = (List<Address>) emsResponse.getResponseData();
                break;
            default:
                addressList = null;
                logger.error("Address findAllError. Code: {}, Description: {}", emsResponse.getCode(), emsResponse.getDescription());
                break;
        }
        return addressList;
    }

    @NeedCleanCoding
    public class CustomerVO extends BaseVO<Customer> {
        private String address;
        private String firstName;
        private String lastName;
        private String email;
        private String name;

        public CustomerVO(Customer customer) {
            super(customer.getId(), customer.getDeleted(), customer);
            this.firstName = customer.getFirstName();
            this.lastName = customer.getLastName();
            this.address = customer.getAddress().getName();
            this.email = customer.getEmailAddress();
            this.name = customer.getName();
        }
    }
}

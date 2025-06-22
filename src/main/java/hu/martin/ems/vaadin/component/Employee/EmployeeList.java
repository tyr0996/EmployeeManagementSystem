package hu.martin.ems.vaadin.component.Employee;

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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.model.User;
import hu.martin.ems.core.vaadin.EmsFilterableGridComponent;
import hu.martin.ems.core.vaadin.ExtraDataFilterField;
import hu.martin.ems.core.vaadin.Switch;
import hu.martin.ems.core.vaadin.TextFilteringHeaderCell;
import hu.martin.ems.model.Employee;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.EmployeeApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
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
@RolesAllowed("ROLE_EmployeeMenuOpenPermission")
@Route(value = "employee/list", layout = MainView.class)
@NeedCleanCoding
public class EmployeeList extends EmsFilterableGridComponent implements Creatable<Employee>, IEmsOptionColumnBaseDialogCreationForm<Employee, EmployeeList.EmployeeVO> {

    @Getter
    private final EmployeeApiClient apiClient = BeanProvider.getBean(EmployeeApiClient.class);
    private final UserApiClient userApi = BeanProvider.getBean(UserApiClient.class);
    private final Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;
    @Getter
    private PaginatedGrid<EmployeeVO, String> grid;
    List<Employee> employees;
    List<EmployeeVO> employeeVOS;

    Grid.Column<EmployeeVO> firstNameColumn;
    Grid.Column<EmployeeVO> lastNameColumn;
    Grid.Column<EmployeeVO> userColumn;
    Grid.Column<EmployeeVO> salaryColumn;
    private Grid.Column<EmployeeVO> extraData;

    private TextFilteringHeaderCell firstNameFilter;
    private TextFilteringHeaderCell lastNameFilter;
    private TextFilteringHeaderCell userFilter;
    private TextFilteringHeaderCell salaryFilter;
    private ExtraDataFilterField extraDataFilter;
    private Logger logger = LoggerFactory.getLogger(Employee.class);
    List<User> userList;
    private LinkedHashMap<String, List<String>> showDeletedCheckboxFilter;

    @Autowired
    public EmployeeList(PaginationSetting paginationSetting) {
        showDeletedCheckboxFilter = new LinkedHashMap<>();
        showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(EmployeeVO.class);
        setEntities();

        firstNameColumn = this.grid.addColumn(v -> v.firstName);
        lastNameColumn = this.grid.addColumn(v -> v.lastName);
        userColumn = this.grid.addColumn(v -> v.user);
        salaryColumn = this.grid.addColumn(v -> v.salary);

        grid.addClassName("styling");
        grid.setPartNameGenerator(employeeVO -> employeeVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        extraData = this.grid.addComponentColumn(employee -> createOptionColumn("Employee", employee));

        setFilteringHeaderRow();
        updateGridItems();

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
            setEntities();
            updateGridItems();
        });

        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedSwitch, create);
//        hl.add(showDeletedCheckbox, create);
//        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, showDeletedSwitch);
        hl.setAlignSelf(Alignment.CENTER, create);



        add(hl, grid);
    }

    public void setEntities() {
        EmsResponse response = apiClient.findAllWithDeleted();
        switch (response.getCode()) {
            case 200:
                employees = (List<Employee>) response.getResponseData();
                break;
            default:
                employees = null;
                logger.error("Employee findAllError. Code: {}, Description: {}", response.getCode(), response.getDescription());
                break;
        }
    }

    private Stream<EmployeeVO> getFilteredStream() {
        return employeeVOS.stream().filter(employeeVO ->
                filterField(firstNameFilter, employeeVO.firstName) &&
                filterField(lastNameFilter, employeeVO.lastName) &&
                filterField(userFilter, employeeVO.user) &&
                filterField(salaryFilter, employeeVO.salary.toString()) &&
                filterExtraData(extraDataFilter, employeeVO, showDeletedCheckboxFilter));
    }

    private void setFilteringHeaderRow() {
        firstNameFilter = new TextFilteringHeaderCell("Search first name...", this);
        lastNameFilter = new TextFilteringHeaderCell("Search last name...", this);
        userFilter = new TextFilteringHeaderCell("Search user...", this);
        salaryFilter = new TextFilteringHeaderCell("Search salary...", this);
        extraDataFilter = new ExtraDataFilterField("", this);

        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(firstNameColumn).setComponent(styleFilterField(firstNameFilter, "First name"));
        filterRow.getCell(lastNameColumn).setComponent(styleFilterField(lastNameFilter, "Last name"));
        filterRow.getCell(userColumn).setComponent(styleFilterField(userFilter, "User"));
        filterRow.getCell(salaryColumn).setComponent(styleFilterField(salaryFilter, "Salary"));
        filterRow.getCell(extraData).setComponent(styleFilterField(extraDataFilter, ""));
    }

    public void updateGridItems() {
        if (employees == null) {
            Notification.show("EmsError happened while getting employees")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            employees = new ArrayList<>();
        }
        employeeVOS = employees.stream().map(EmployeeVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }

    public EmsDialog getSaveOrUpdateDialog(EmployeeVO entity) {
        EmsDialog createDialog = new EmsDialog((entity == null ? "Create" : "Modify") + " employee");

        FormLayout formLayout = new FormLayout();

        Button saveButton = new Button("Save");

        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        NumberField salaryField = new NumberField("Salary");
        EmsComboBox<User> users = new EmsComboBox<>("User", this::setupUsers, saveButton, "EmsError happened while getting users");
//        setupUsers();
//        ComboBox<User> users = new ComboBox<>("User");
//        ComboBox.ItemFilter<User> filter = (user, filterString) ->
//                user.getUsername().toLowerCase().contains(filterString.toLowerCase());
//        if (userList == null) {
//            users.setEnabled(false);
//            users.setErrorMessage("EmsError happened while getting users");
//            users.setInvalid(true);
//            saveButton.setEnabled(false);
//        } else {
//            users.setItems(filter, userList);
//            users.setItemLabelGenerator(User::getUsername);
//        }

        if (entity != null) {
            firstNameField.setValue(entity.original.getFirstName());
            lastNameField.setValue(entity.original.getLastName());
            salaryField.setValue(entity.original.getSalary().doubleValue());
            users.setValue(entity.original.getUser());
        }

        saveButton.addClickListener(event -> {
            Employee employee = Optional.ofNullable(entity)
                    .map(e -> e.original)
                    .orElseGet(Employee::new);
            employee.setFirstName(firstNameField.getValue());
            employee.setLastName(lastNameField.getValue());
            employee.setDeleted(0L);
            employee.setSalary(salaryField.getValue().intValue());
            employee.setUser(users.getValue());
            EmsResponse response = null;
            if (entity != null) {
                response = apiClient.update(employee);
            } else {
                response = apiClient.save(employee);
            }
            switch (response.getCode()) {
                case 200: {
                    Notification.show("Employee " + (entity == null ? "saved: " : "updated: ") + employee)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                }
                default: {
                    Notification.show("Employee " + (entity == null ? "saving" : "modifying") + " failed: " + response.getDescription())
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    setEntities();
                    updateGridItems();
                    return;
                }
            }

            firstNameField.clear();
            lastNameField.clear();
            salaryField.clear();
            users.clear();
            createDialog.close();
            setEntities();
            updateGridItems();
        });

        formLayout.add(firstNameField, lastNameField, salaryField, users, saveButton);

        createDialog.add(formLayout);
        return createDialog;
    }

    private List<User> setupUsers() {
        EmsResponse emsResponse = userApi.findAll();
        switch (emsResponse.getCode()) {
            case 200:
                userList = (List<User>) emsResponse.getResponseData();
                break;
            default:
                userList = null;
                logger.error("User findAllError. Code: {}, Description: {}", emsResponse.getCode(), emsResponse.getDescription());
                break;
        }
        return userList;
    }

    @NeedCleanCoding
    public class EmployeeVO extends BaseVO<Employee> {
        private String firstName;
        private String lastName;
        private String user;
        private Integer salary;

        public EmployeeVO(Employee employee) {
            super(employee.id, employee.getDeleted(), employee);
            this.id = employee.getId();
            this.deleted = employee.getDeleted();
            this.firstName = original.getFirstName();
            this.lastName = original.getLastName();
            this.user = original.getUser().getUsername();
            this.salary = original.getSalary();
        }
    }
}

package hu.martin.ems.vaadin.component.Employee;

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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.core.model.User;
import hu.martin.ems.model.Employee;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.EmployeeApiClient;
import hu.martin.ems.vaadin.api.UserApiClient;
import hu.martin.ems.vaadin.component.BaseVO;
import hu.martin.ems.vaadin.component.Creatable;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@RolesAllowed("ROLE_EmployeeMenuOpenPermission")
@Route(value = "employee/list", layout = MainView.class)
@NeedCleanCoding
public class EmployeeList extends VerticalLayout implements Creatable<Employee> {

    private final EmployeeApiClient employeeApi = BeanProvider.getBean(EmployeeApiClient.class);
    private final UserApiClient userApi = BeanProvider.getBean(UserApiClient.class);
    private final Gson gson = BeanProvider.getBean(Gson.class);
    private boolean showDeleted = false;
    private PaginatedGrid<EmployeeVO, String> grid;
    private final PaginationSetting paginationSetting;
    List<Employee> employees;
    List<EmployeeVO> employeeVOS;

    Grid.Column<EmployeeVO> firstNameColumn;
    Grid.Column<EmployeeVO> lastNameColumn;
    Grid.Column<EmployeeVO> userColumn;
    Grid.Column<EmployeeVO> salaryColumn;
    private LinkedHashMap<String, List<String>> mergedFilterMap = new LinkedHashMap<>();
    private Grid.Column<EmployeeVO> extraData;

    private String firstNameFilterText = "";
    private String lastNameFilterText = "";
    private String userFilterText = "";
    private String salaryFilterText = "";
    private Logger logger = LoggerFactory.getLogger(Employee.class);
    List<User> userList;
    private MainView mainView;
    @Autowired
    public EmployeeList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;
        EmployeeVO.showDeletedCheckboxFilter.put("deleted", Arrays.asList("0"));

        this.grid = new PaginatedGrid<>(EmployeeVO.class);
        setupEmployees();
        updateGridItems();

//        this.grid.removeAllColumns(); // TODO megnézni az összesnél, hogy így nézzen ki
        firstNameColumn = this.grid.addColumn(v -> v.firstName);
        lastNameColumn = this.grid.addColumn(v -> v.lastName);
        userColumn = this.grid.addColumn(v -> v.user);
        salaryColumn = this.grid.addColumn(v -> v.salary);

        grid.addClassName("styling");
        grid.setPartNameGenerator(employeeVO -> employeeVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        //region Options column
        extraData = this.grid.addComponentColumn(employee -> {
            Button editButton = new Button(EDIT.create());
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
            Button restoreButton = new Button(VaadinIcon.BACKWARDS.create());
            restoreButton.addClassNames("info_button_variant");
            Button permanentDeleteButton = new Button(PERMANENTLY_DELETE.create());
            permanentDeleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

            editButton.addClickListener(event -> {
                Dialog dialog = getSaveOrUpdateDialog(employee.original);
                dialog.open();
            });

            restoreButton.addClickListener(event -> {
                this.employeeApi.restore(employee.original);
                Notification.show("Employee restored: " + employee.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupEmployees();
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                EmsResponse resp = this.employeeApi.delete(employee.original);
                switch (resp.getCode()){
                    case 200: {
                        Notification.show("Employee deleted: " + employee.original.getName())
                                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        updateGridItems();
                        break;
                    }
                    default: {
                        Notification.show(resp.getDescription()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    }
                }
                setupEmployees();
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.employeeApi.permanentlyDelete(employee.original.getId());
                Notification.show("Employee permanently deleted: " + employee.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                setupEmployees();
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (employee.original.getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else {
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
            EmployeeVO.showDeletedCheckboxFilter.replace("deleted", newValue);
            setupEmployees();
            updateGridItems();
        });
        HorizontalLayout hl = new HorizontalLayout();
        hl.add(showDeletedCheckbox, create);
        hl.setAlignSelf(Alignment.CENTER, showDeletedCheckbox);
        hl.setAlignSelf(Alignment.CENTER, create);

        add(hl, grid);
    }

    private void setupEmployees() {
        EmsResponse response = employeeApi.findAllWithDeleted();
        switch (response.getCode()){
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
                (firstNameFilterText.isEmpty() || employeeVO.firstName.toLowerCase().contains(firstNameFilterText.toLowerCase())) &&
                (lastNameFilterText.isEmpty() || employeeVO.lastName.toLowerCase().contains(lastNameFilterText.toLowerCase())) &&
                (userFilterText.isEmpty() || employeeVO.user.toLowerCase().contains(userFilterText.toLowerCase())) &&
                (salaryFilterText.isEmpty() || employeeVO.salary.toString().toLowerCase().contains(salaryFilterText.toLowerCase())) &&
                employeeVO.filterExtraData()
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

        TextField userFilter = new TextField();
        userFilter.setPlaceholder("Search user...");
        userFilter.setClearButtonVisible(true);
        userFilter.addValueChangeListener(event -> {
            userFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField salaryFilter = new TextField();
        salaryFilter.setPlaceholder("Search salary...");
        salaryFilter.setClearButtonVisible(true);
        salaryFilter.addValueChangeListener(event -> {
            salaryFilterText = event.getValue().trim();
            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        TextField extraDataFilter = new TextField();
        extraDataFilter.addKeyDownListener(Key.ENTER, event -> {
            if(extraDataFilter.getValue().isEmpty()){
                EmployeeVO.extraDataFilterMap.clear();
            }
            else{
                EmployeeVO.extraDataFilterMap = gson.fromJson(extraDataFilter.getValue().trim(), LinkedHashMap.class);
            }

            grid.getDataProvider().refreshAll();
            updateGridItems();
        });

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();
        filterRow.getCell(firstNameColumn).setComponent(filterField(firstNameFilter, "First name"));
        filterRow.getCell(lastNameColumn).setComponent(filterField(lastNameFilter, "Last name"));
        filterRow.getCell(userColumn).setComponent(filterField(userFilter, "User"));
        filterRow.getCell(salaryColumn).setComponent(filterField(salaryFilter, "Salary"));
        filterRow.getCell(extraData).setComponent(filterField(extraDataFilter, ""));
    }

    private void updateGridItems() {
        if(employees == null){
            Notification.show("Error happened while getting employees")
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            employees = new ArrayList<>();
        }
        employeeVOS = employees.stream().map(EmployeeVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }

    private void appendCloseButton(Dialog d){
        Button closeButton = new Button(new Icon("lumo", "cross"),
                (e) -> d.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        d.getHeader().add(closeButton);
    }

    public Dialog getSaveOrUpdateDialog(Employee entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " employee");
        appendCloseButton(createDialog);
        FormLayout formLayout = new FormLayout();

        Button saveButton = new Button("Save");

        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        NumberField salaryField = new NumberField("Salary");

        setupUsers();
        ComboBox<User> users = new ComboBox<>("User");
        ComboBox.ItemFilter<User> filter = (user, filterString) ->
                user.getUsername().toLowerCase().contains(filterString.toLowerCase());
        if(userList == null){
            users.setEnabled(false);
            users.setErrorMessage("Error happened while getting users");
            users.setInvalid(true);
            saveButton.setEnabled(false);
        }
        else{
            users.setItems(filter, userList);
            users.setItemLabelGenerator(User::getUsername);
        }

        if (entity != null) {
            firstNameField.setValue(entity.getFirstName());
            lastNameField.setValue(entity.getLastName());
            salaryField.setValue(entity.getSalary().doubleValue());
            users.setValue(entity.getUser());
        }

        saveButton.addClickListener(event -> {
            Employee employee = Objects.requireNonNullElseGet(entity, Employee::new);
            employee.setFirstName(firstNameField.getValue());
            employee.setLastName(lastNameField.getValue());
            employee.setDeleted(0L);
            employee.setSalary(salaryField.getValue().intValue());
            employee.setUser(users.getValue());
            EmsResponse response = null;
            if(entity != null){
                response = employeeApi.update(employee);
            }
            else{
                response = employeeApi.save(employee);
            }
            switch (response.getCode()){
                case 200: {
                    Notification.show("Employee " + (entity == null ? "saved: " : "updated: ") + employee)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    break;
                }
                default: {
                    Notification.show("Employee " + (entity == null ? "saving" : "modifying") + " failed: " + response.getDescription())
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    createDialog.close();
                    setupEmployees();
                    updateGridItems();
                    return;
                }
            }

            firstNameField.clear();
            lastNameField.clear();
            salaryField.clear();
            users.clear();
            createDialog.close();
            setupEmployees();
            updateGridItems();
        });

        formLayout.add(firstNameField, lastNameField, salaryField, users, saveButton);

        createDialog.add(formLayout);
        return createDialog;
    }

    private void setupUsers() {
        EmsResponse emsResponse = userApi.findAll();
        switch (emsResponse.getCode()){
            case 200:
                userList = (List<User>) emsResponse.getResponseData();
                break;
            default:
                userList = null;
                logger.error("User findAllError. Code: {}, Description: {}", emsResponse.getCode(), emsResponse.getDescription());
                break;
        }
    }

    @NeedCleanCoding
public class EmployeeVO extends BaseVO {
        private Employee original;
        private String firstName;
        private String lastName;
        private String user;
        private Integer salary;

        public EmployeeVO(Employee employee) {
            super(employee.id, employee.getDeleted());
            this.original = employee;
            this.id = employee.getId();
            this.deleted = employee.getDeleted();
            this.firstName = original.getFirstName();
            this.lastName = original.getLastName();
            this.user = original.getUser().getUsername();
            this.salary = original.getSalary();
        }
    }
}

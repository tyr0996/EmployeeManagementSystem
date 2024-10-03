package hu.martin.ems.vaadin.component.Employee;

import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.model.PaginationSetting;
import hu.martin.ems.model.Employee;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.MainView;
import hu.martin.ems.vaadin.api.EmployeeApiClient;
import hu.martin.ems.vaadin.api.RoleApiClient;
import hu.martin.ems.vaadin.component.City.CityList;
import hu.martin.ems.vaadin.component.Creatable;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hu.martin.ems.core.config.StaticDatas.Icons.EDIT;
import static hu.martin.ems.core.config.StaticDatas.Icons.PERMANENTLY_DELETE;

@CssImport("./styles/ButtonVariant.css")
@CssImport("./styles/grid.css")
@AnonymousAllowed
@Route(value = "employee/list", layout = MainView.class)
@NeedCleanCoding
public class EmployeeList extends VerticalLayout implements Creatable<Employee> {

    private final EmployeeApiClient employeeApi = BeanProvider.getBean(EmployeeApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private boolean showDeleted = false;
    private PaginatedGrid<EmployeeVO, String> grid;
    private final PaginationSetting paginationSetting;
    List<Employee> employees;
    List<EmployeeVO> employeeVOS;

    Grid.Column<EmployeeVO> firstNameColumn;
    Grid.Column<EmployeeVO> lastNameColumn;
    Grid.Column<EmployeeVO> roleColumn;
    Grid.Column<EmployeeVO> salaryColumn;

    private String firstNameFilterText = "";
    private String lastNameFilterText = "";
    private String roleFilterText = "";
    private String salaryFilterText = "";
    @Autowired
    public EmployeeList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(EmployeeVO.class);
        employees = employeeApi.findAll();
        employeeVOS = employees.stream().map(EmployeeVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().toList());

//        this.grid.removeAllColumns(); // TODO megnézni az összesnél, hogy így nézzen ki
        firstNameColumn = this.grid.addColumn(v -> v.firstName);
        lastNameColumn = this.grid.addColumn(v -> v.lastName);
        roleColumn = this.grid.addColumn(v -> v.role);
        salaryColumn = this.grid.addColumn(v -> v.salary);

        grid.addClassName("styling");
        grid.setPartNameGenerator(employeeVO -> employeeVO.deleted != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

        setFilteringHeaderRow();

        //region Options column
        this.grid.addComponentColumn(employee -> {
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
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.employeeApi.delete(employee.original);
                Notification.show("Employee deleted: " + employee.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.employeeApi.permanentlyDelete(employee.original.getId());
                Notification.show("Employee permanently deleted: " + employee.original.getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (employee.original.getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (employee.original.getDeleted() == 1) {
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

    private Stream<EmployeeVO> getFilteredStream() {

        return employeeVOS.stream().filter(employeeVO ->
                (firstNameFilterText.isEmpty() || employeeVO.firstName.toLowerCase().contains(firstNameFilterText.toLowerCase())) &&
                (lastNameFilterText.isEmpty() || employeeVO.lastName.toLowerCase().contains(lastNameFilterText.toLowerCase())) &&
                (roleFilterText.isEmpty() || employeeVO.role.toLowerCase().contains(roleFilterText.toLowerCase())) &&
                (salaryFilterText.isEmpty() || employeeVO.salary.toString().toLowerCase().contains(salaryFilterText.toLowerCase())) &&
                (showDeleted ? (employeeVO.deleted == 0 || employeeVO.deleted == 1) : employeeVO.deleted == 0)
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

        TextField roleFilter = new TextField();
        roleFilter.setPlaceholder("Search role...");
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(event -> {
            roleFilterText = event.getValue().trim();
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

        // Header-row hozzáadása a Grid-hez és a szűrők elhelyezése
        HeaderRow filterRow = grid.appendHeaderRow();;
        filterRow.getCell(firstNameColumn).setComponent(filterField(firstNameFilter, "First name"));
        filterRow.getCell(lastNameColumn).setComponent(filterField(lastNameFilter, "Last name"));
        filterRow.getCell(roleColumn).setComponent(filterField(roleFilter, "Role"));
        filterRow.getCell(salaryColumn).setComponent(filterField(salaryFilter, "Salary"));
    }

    private void updateGridItems() {
        List<Employee> employees = employeeApi.findAllWithDeleted();
        employeeVOS = employees.stream().map(EmployeeVO::new).collect(Collectors.toList());
        this.grid.setItems(getFilteredStream().collect(Collectors.toList()));
    }

    public Dialog getSaveOrUpdateDialog(Employee entity) {
        Dialog createDialog = new Dialog((entity == null ? "Create" : "Modify") + " employee");
        FormLayout formLayout = new FormLayout();

        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        NumberField salaryField = new NumberField("Salary");
        ComboBox<Role> roles = new ComboBox<>("Role");
        ComboBox.ItemFilter<Role> filter = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        roles.setItems(filter, roleApi.findAll());
        roles.setItemLabelGenerator(Role::getName);

        if (entity != null) {
            firstNameField.setValue(entity.getFirstName());
            lastNameField.setValue(entity.getLastName());
            salaryField.setValue(entity.getSalary().doubleValue());
            roles.setValue(entity.getRole());
        }

        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> {
            Employee employee = Objects.requireNonNullElseGet(entity, Employee::new);
            employee.setFirstName(firstNameField.getValue());
            employee.setLastName(lastNameField.getValue());
            employee.setDeleted(0L);
            employee.setSalary(salaryField.getValue().intValue());
            employee.setRole(roles.getValue());
            if(entity != null){
                employeeApi.update(employee);
            }
            else{
                employeeApi.save(employee);
            }
            Notification.show("Employee " + (entity == null ? "saved: " : "updated: ") + employee.getFirstName() + " " + employee.getLastName())
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            firstNameField.clear();
            lastNameField.clear();
            salaryField.clear();
            roles.clear();
            createDialog.close();
            updateGridItems();
        });

        formLayout.add(firstNameField, lastNameField, salaryField, roles, saveButton);

        createDialog.add(formLayout);
        return createDialog;
    }

    @NeedCleanCoding
public class EmployeeVO {
        private Employee original;
        private Long deleted;
        private Long id;
        private String firstName;
        private String lastName;
        private String role;
        private Integer salary;

        public EmployeeVO(Employee employee) {
            this.original = employee;
            this.id = employee.getId();
            this.deleted = employee.getDeleted();
            this.firstName = original.getFirstName();
            this.lastName = original.getLastName();
            this.role = original.getRole().getName();
            this.salary = original.getSalary();
        }
    }
}

package hu.martin.ems.vaadin.component.Employee;

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
@AnonymousAllowed
@Route(value = "employee/list", layout = MainView.class)
@NeedCleanCoding
public class EmployeeList extends VerticalLayout implements Creatable<Employee> {

    private final EmployeeApiClient employeeApi = BeanProvider.getBean(EmployeeApiClient.class);
    private final RoleApiClient roleApi = BeanProvider.getBean(RoleApiClient.class);
    private boolean showDeleted = false;
    private PaginatedGrid<EmployeeVO, String> grid;
    private final PaginationSetting paginationSetting;

    @Autowired
    public EmployeeList(PaginationSetting paginationSetting) {
        this.paginationSetting = paginationSetting;

        this.grid = new PaginatedGrid<>(EmployeeVO.class);
        List<Employee> employees = employeeApi.findAll();
        List<EmployeeVO> data = employees.stream().map(EmployeeVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");
        grid.addClassName("styling");
        grid.setPartNameGenerator(employeeVO -> employeeVO.getDeleted() != 0 ? "deleted" : null);
        grid.setPageSize(paginationSetting.getPageSize());
        grid.setPaginationLocation(paginationSetting.getPaginationLocation());

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
                this.employeeApi.restore(employee.getOriginal());
                Notification.show("Employee restored: " + employee.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.employeeApi.delete(employee.getOriginal());
                Notification.show("Employee deleted: " + employee.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.employeeApi.permanentlyDelete(employee.getOriginal().getId());
                Notification.show("Employee permanently deleted: " + employee.getOriginal().getName())
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if (employee.getOriginal().getDeleted() == 0) {
                actions.add(editButton, deleteButton);
            } else if (employee.getOriginal().getDeleted() == 1) {
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
        List<Employee> employees = showDeleted ? employeeApi.findAllWithDeleted() : employeeApi.findAll();
        this.grid.setItems(employees.stream().map(EmployeeVO::new).collect(Collectors.toList()));
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
            Notification.show("Employee saved: " + employee.getFirstName() + " " + employee.getLastName())
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

    @Getter
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

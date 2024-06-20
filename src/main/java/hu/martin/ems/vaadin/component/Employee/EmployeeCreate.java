package hu.martin.ems.vaadin.component.Employee;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Employee;
import hu.martin.ems.model.Role;
import hu.martin.ems.service.EmployeeService;
import hu.martin.ems.service.RoleService;
import hu.martin.ems.vaadin.MainView;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

@Route(value = "employee/create", layout = MainView.class)
public class EmployeeCreate extends VerticalLayout {

    private final EmployeeService employeeService;
    private final RoleService roleService;

    public static Employee e;

    @Autowired
    public EmployeeCreate(EmployeeService employeeService,
                          RoleService roleService) {
        this.employeeService = employeeService;
        this.roleService = roleService;
        FormLayout formLayout = new FormLayout();

        TextField firstNameField = new TextField("First Name");
        TextField lastNameField = new TextField("Last Name");
        NumberField salaryField = new NumberField("Salary");
        ComboBox<Role> roles = new ComboBox<>("Role");
        ComboBox.ItemFilter<Role> filter = (role, filterString) ->
                role.getName().toLowerCase().contains(filterString.toLowerCase());
        roles.setItems(filter, roleService.findAll(false));
        roles.setItemLabelGenerator(Role::getName);

        if (e != null) {
            firstNameField.setValue(e.getFirstName());
            lastNameField.setValue(e.getLastName());
            salaryField.setValue(e.getSalary().doubleValue());
            roles.setValue(e.getRole());
        }

        Button saveButton = new Button("Save");

        saveButton.addClickListener(event -> {
            Employee employee = Objects.requireNonNullElseGet(e, Employee::new);
            employee.setFirstName(firstNameField.getValue());
            employee.setLastName(lastNameField.getValue());
            employee.setDeleted(0L);
            employee.setSalary(salaryField.getValue().intValue());
            employee.setRole(roles.getValue());
            employeeService.saveOrUpdate(employee);

            Notification.show("Employee saved: " + employee.getFirstName() + " " + employee.getLastName());

            e = null;
            firstNameField.clear();
            lastNameField.clear();
            salaryField.clear();
            roles.clear();
        });

        formLayout.add(firstNameField, lastNameField, salaryField, roles, saveButton);

        add(formLayout);
    }
}


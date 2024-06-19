package hu.martin.ems.vaadin.component.Employee;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import hu.martin.ems.model.Employee;
import hu.martin.ems.service.EmployeeService;
import hu.martin.ems.vaadin.MainView;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "employee/list", layout = MainView.class)
public class EmployeeList extends VerticalLayout {

    private final EmployeeService employeeService;
    private boolean showDeleted = false;
    private Grid<EmployeeVO> grid;

    @Autowired
    public EmployeeList(EmployeeService employeeService) {
        this.employeeService = employeeService;

        this.grid = new Grid<>(EmployeeVO.class);
        List<Employee> employees = employeeService.findAll(false);
        List<EmployeeVO> data = employees.stream().map(EmployeeVO::new).collect(Collectors.toList());
        this.grid.setItems(data);
        this.grid.removeColumnByKey("original");

        //region Options column
        this.grid.addComponentColumn(employee -> {
            Button editButton = new Button("Edit");
            Button deleteButton = new Button("Delete");
            Button restoreButton = new Button("Restore");
            Button permanentDeleteButton = new Button("Permanently Delete");

            editButton.addClickListener(event -> {
                EmployeeCreate.e = employee.getOriginal();
                UI.getCurrent().navigate("employee/create");
            });

            restoreButton.addClickListener(event -> {
                this.employeeService.restore(employee.getOriginal());
                Notification.show("Employee restored: " + employee.getOriginal().getName());
                updateGridItems();
            });

            deleteButton.addClickListener(event -> {
                this.employeeService.delete(employee.getOriginal());
                Notification.show("Employee deleted: " + employee.getOriginal().getName());
                updateGridItems();
            });

            permanentDeleteButton.addClickListener(event -> {
                this.employeeService.permanentlyDelete(employee.getOriginal());
                Notification.show("Employee permanently deleted: " + employee.getOriginal().getName());
                updateGridItems();
            });

            HorizontalLayout actions = new HorizontalLayout();
            if(employee.getOriginal().getDeleted() == 0){ actions.add(editButton, deleteButton); }
            else if(employee.getOriginal().getDeleted() == 1){ actions.add(permanentDeleteButton, restoreButton); }
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
        List<Employee> employees = this.employeeService.findAll(showDeleted);
        this.grid.setItems(employees.stream().map(EmployeeVO::new).collect(Collectors.toList()));
    }

    @Getter
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

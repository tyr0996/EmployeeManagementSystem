package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeApiClient extends EmsApiClient<Employee> {
    public EmployeeApiClient() {
        super(Employee.class);
    }
}

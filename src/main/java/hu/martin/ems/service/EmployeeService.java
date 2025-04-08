package hu.martin.ems.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Employee;
import hu.martin.ems.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

@Service
@NeedCleanCoding
public class EmployeeService extends BaseService<Employee, EmployeeRepository> {
    public EmployeeService(EmployeeRepository employeeRepository) {
        super(employeeRepository);
    }
}
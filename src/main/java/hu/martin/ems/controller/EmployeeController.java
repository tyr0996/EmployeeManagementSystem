package hu.martin.ems.controller;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Employee;
import hu.martin.ems.repository.EmployeeRepository;
import hu.martin.ems.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/employee")
@NeedCleanCoding
public class EmployeeController extends BaseController<Employee, EmployeeService, EmployeeRepository> {
    public EmployeeController(EmployeeService service) {
        super(service);
    }
}

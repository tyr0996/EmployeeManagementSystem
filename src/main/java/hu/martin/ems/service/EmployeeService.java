package hu.martin.ems.service;

import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Employee;
import hu.martin.ems.repository.EmployeeRepository;
import hu.martin.ems.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
public class EmployeeService extends BaseService<Employee, EmployeeRepository> {


    private static final String[] FIRST_NAMES = {
            "Kovács", "Szabó", "Nagy", "Tóth", "Varga", "Horváth", "Kiss", "Molnár",
            "Németh", "Farkas", "Balogh", "Papp", "Takács", "Juhász", "Mészáros",
            "Simon", "Rácz", "Fekete", "Szalai", "Lukács"
    };
    private static final String[] LAST_NAMES = {
            "Bence", "Gergő", "Levente", "Máté", "Dominik", "Ádám", "Balázs", "Attila",
            "Zoltán", "László", "János", "István", "Gábor", "András", "Tamás",
            "Péter", "Miklós", "Ferenc", "Imre", "György"
    };


    private static final Random RANDOM = new Random();

    private RoleRepository roleRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           RoleRepository roleRepository) {
        super(employeeRepository);
        this.roleRepository = roleRepository;
    }
}
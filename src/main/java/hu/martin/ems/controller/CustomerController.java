package hu.martin.ems.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Customer;
import hu.martin.ems.repository.CustomerRepository;
import hu.martin.ems.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/customer")
@AnonymousAllowed
public class CustomerController extends BaseController<Customer, CustomerService, CustomerRepository> {
    public CustomerController(CustomerService service) {
        super(service);
    }
}

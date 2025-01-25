package hu.martin.ems.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Customer;
import hu.martin.ems.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
@NeedCleanCoding
public class CustomerService extends BaseService<Customer, CustomerRepository> {
    public CustomerService(CustomerRepository customerRepository) {
        super(customerRepository);
    }
}

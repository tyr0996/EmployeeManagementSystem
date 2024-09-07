package hu.martin.ems.service;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Customer;
import hu.martin.ems.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@NeedCleanCoding
public class CustomerService extends BaseService<Customer, CustomerRepository> {
    public CustomerService(CustomerRepository customerRepository) {
        super(customerRepository);
    }
}

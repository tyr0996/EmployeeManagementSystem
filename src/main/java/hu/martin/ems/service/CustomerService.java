package hu.martin.ems.service;

import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Customer;
import hu.martin.ems.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CustomerService extends BaseService<Customer, CustomerRepository>{
    public CustomerService(CustomerRepository customerRepository) { super(customerRepository); }
}

package hu.martin.ems.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Address;
import hu.martin.ems.repository.AddressRepository;
import org.springframework.stereotype.Service;

@Service
@NeedCleanCoding
public class AddressService extends BaseService<Address, AddressRepository> {
    public AddressService(AddressRepository repo) {
        super(repo);
    }
}

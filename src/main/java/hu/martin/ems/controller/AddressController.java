package hu.martin.ems.controller;

import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Address;
import hu.martin.ems.repository.AddressRepository;
import hu.martin.ems.service.AddressService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/address")
public class AddressController extends BaseController<Address, AddressService, AddressRepository> {

    public AddressController(AddressService service) {
        super(service);
    }

}

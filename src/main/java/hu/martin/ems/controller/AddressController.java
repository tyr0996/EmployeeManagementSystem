package hu.martin.ems.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Address;
import hu.martin.ems.repository.AddressRepository;
import hu.martin.ems.service.AddressService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/address")
@AnonymousAllowed
public class AddressController extends BaseController<Address, AddressService, AddressRepository> {

    public AddressController(AddressService service) {
        super(service);
    }
}

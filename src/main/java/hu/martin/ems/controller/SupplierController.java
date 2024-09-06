package hu.martin.ems.controller;

import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Supplier;
import hu.martin.ems.repository.SupplierRepository;
import hu.martin.ems.service.SupplierService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/supplier")
public class SupplierController extends BaseController<Supplier, SupplierService, SupplierRepository> {
    public SupplierController(SupplierService service) {
        super(service);
    }
}

package hu.martin.ems.controller;

import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.OrderElementRepository;
import hu.martin.ems.service.OrderElementService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/api/orderElement")
public class OrderElementController extends BaseController<OrderElement, OrderElementService, OrderElementRepository> {
    public OrderElementController(OrderElementService service) {
        super(service);
    }

    @GetMapping("/getByCustomer")
    public ResponseEntity<String> getByCustomer(@Param("customerId") Long customerId) {
        return new ResponseEntity<>(gson.toJson(service.getByCustomer(customerId)), HttpStatus.OK);
    }

    @GetMapping("/getBySupplier")
    public ResponseEntity<String> getBySupplier(@Param("supplierId") Long supplierId) {
        return new ResponseEntity<>(gson.toJson(service.getBySupplier(supplierId)), HttpStatus.OK);
    }

    @GetMapping(path = "/findAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> findAll(@RequestParam(required = false, defaultValue = "false") Boolean withDeleted) {
        List<OrderElement> allElements = service.findAll(withDeleted);
        return new ResponseEntity<>(gson.toJson(allElements), HttpStatus.OK);
    }
}

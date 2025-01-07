package hu.martin.ems.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.OrderElementRepository;
import hu.martin.ems.service.OrderElementService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> getByCustomer(@Param("customerId") Long customerId) throws JsonProcessingException {
        return new ResponseEntity<>(gson.toJson(service.getByCustomer(customerId)), HttpStatus.OK);
    }

    @Override
    @GetMapping(path = "/findAll", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> findAll(@RequestParam(required = false, defaultValue = "false") Boolean withDeleted) {
        List<OrderElement> allElements = service.findAll(withDeleted);
        return new ResponseEntity<>(gson.toJson(allElements), HttpStatus.OK);
    }

}

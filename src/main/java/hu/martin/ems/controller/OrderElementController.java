package hu.martin.ems.controller;

import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.OrderElement;
import hu.martin.ems.repository.OrderElementRepository;
import hu.martin.ems.service.OrderElementService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/orderElement")
public class OrderElementController extends BaseController<OrderElement, OrderElementService, OrderElementRepository> {
    public OrderElementController(OrderElementService service) {
        super(service);
    }
}

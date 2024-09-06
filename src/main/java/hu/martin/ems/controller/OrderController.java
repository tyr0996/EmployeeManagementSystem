package hu.martin.ems.controller;

import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Order;
import hu.martin.ems.repository.OrderRepository;
import hu.martin.ems.service.OrderService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController extends BaseController<Order, OrderService, OrderRepository> {
    public OrderController(OrderService service) {
        super(service);
    }
}

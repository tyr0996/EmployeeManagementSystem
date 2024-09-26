package hu.martin.ems.controller;

import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Order;
import hu.martin.ems.repository.OrderRepository;
import hu.martin.ems.service.OrderElementService;
import hu.martin.ems.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/order")
public class OrderController extends BaseController<Order, OrderService, OrderRepository> {
    public OrderController(OrderService service) {
        super(service);
    }

    @Autowired
    private OrderElementService orderElementService;

    @PostMapping(path = "/createDocumentAsODT")
    public ResponseEntity<byte[]> createDocumentAsODT(@RequestBody Order order) {
        return new ResponseEntity<>(service.createDocumentAsODT(order, new ByteArrayOutputStream()), HttpStatus.OK);
    }

    @PostMapping(path = "/createDocumentAsPDF")
    public ResponseEntity<byte[]> createDocumentAsPDF(@RequestBody Order order) {
        return new ResponseEntity<>(service.createDocumentAsPDF(order, new ByteArrayOutputStream()), HttpStatus.OK);
    }

    @PostMapping(path = "/generateHTMLEmail")
    public ResponseEntity<String> generateHTMLEmail(@RequestBody Order order){
        return new ResponseEntity<>(service.generateHTMLEmail(order), HttpStatus.OK);
    }

    @PostMapping(path = "/sendReport")
    public ResponseEntity<String> sendReport(@RequestBody LocalDate from, LocalDate to) {
        service.sendReport(from, to);
        return new ResponseEntity<>("", HttpStatus.OK); //TODO
    }

    @Override
    @DeleteMapping(path = "/permanentlyDelete", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> permanentlyDelete(@RequestParam(value = "id") Long entityId) {
        orderElementService.permanentlyDeleteByOrder(entityId);
        service.permanentlyDelete(entityId);
        return new ResponseEntity<>("{\"response\":\"ok\"}", HttpStatus.OK);
    }
}

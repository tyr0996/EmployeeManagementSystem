package hu.martin.ems.controller;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Order;
import hu.martin.ems.repository.OrderRepository;
import hu.martin.ems.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/order")
@NeedCleanCoding
public class OrderController extends BaseController<Order, OrderService, OrderRepository> {
    public OrderController(OrderService service) {
        super(service);
    }

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
}

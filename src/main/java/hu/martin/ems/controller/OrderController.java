package hu.martin.ems.controller;

import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.core.model.EmsResponse;
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
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

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

    @PutMapping(path = "/sendReportSFTPToAccountant")
    public ResponseEntity<String> sendReportSFTPToAccountant(@RequestBody LinkedHashMap<String, LocalDate> data) {
        boolean res = service.sendReportSFTPToAccountant(data.get("from"), data.get("to"));
        if(res){
            return new ResponseEntity<>(EmsResponse.Description.SFTP_SENDING_SUCCESS, HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(EmsResponse.Description.SFTP_SENDING_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DeleteMapping(path = "/permanentlyDelete", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> permanentlyDelete(@RequestParam(value = "id") Long entityId) {
        orderElementService.permanentlyDeleteByOrder(entityId);
        service.permanentlyDelete(entityId);
        return new ResponseEntity<>("{\"response\":\"ok\"}", HttpStatus.OK);
    }
}

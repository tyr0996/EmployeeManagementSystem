package hu.martin.ems.controller;

import fr.opensagres.xdocreport.core.XDocReportException;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Order;
import hu.martin.ems.repository.OrderRepository;
import hu.martin.ems.service.OrderElementService;
import hu.martin.ems.service.OrderService;
import hu.martin.ems.vaadin.api.Error;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
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
        try{
            return new ResponseEntity<>(service.createDocumentAsODT(order, new ByteArrayOutputStream()), HttpStatus.OK);
        }
        catch(IOException e){
            return new ResponseEntity<>(EmsResponse.Description.DOCUMENT_GENERATION_FAILED_MISSING_TEMPLATE.getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (XDocReportException e){
            return new ResponseEntity<>(EmsResponse.Description.DOCUMENT_GENERATION_FAILED_NOT_SUPPORTED_FILE_TYPE.getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/createDocumentAsPDF")
    public ResponseEntity<byte[]> createDocumentAsPDF(@RequestBody Order order) {
        try{
            byte[] res = service.createDocumentAsPDF(order, new ByteArrayOutputStream());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        catch(IOException e){
            return new ResponseEntity<>(EmsResponse.Description.DOCUMENT_GENERATION_FAILED_MISSING_TEMPLATE.getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (XDocReportException e){
            return new ResponseEntity<>(EmsResponse.Description.DOCUMENT_GENERATION_FAILED_NOT_SUPPORTED_FILE_TYPE.getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/generateHTMLEmail")
    public ResponseEntity<String> generateHTMLEmail(@RequestParam Long orderId){
        try{
            return new ResponseEntity<>(service.generateHTMLEmail(orderId), HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(path = "/sendReportSFTPToAccountant")
    public ResponseEntity<String> sendReportSFTPToAccountant(@RequestBody LinkedHashMap<String, LocalDate> data) {
        try{
            boolean res = service.sendReportSFTPToAccountant(data.get("from"), data.get("to"));
            if(res){
                return new ResponseEntity<>(EmsResponse.Description.SFTP_SENDING_SUCCESS, HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(gson.toJson(new Error(Instant.now().toEpochMilli(), 500, EmsResponse.Description.SFTP_SENDING_ERROR, "/api/order/sendReportSFTPToAccountant")), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        catch (IOException e){
            return new ResponseEntity<>(gson.toJson(new Error(Instant.now().toEpochMilli(), 500, "XLS generation failed", "/api/order/sendReportSFTPToAccountant")), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DeleteMapping(path = "/permanentlyDelete", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> permanentlyDelete(@RequestParam(value = "id") Long entityId) {
        orderElementService.permanentlyDeleteByOrder(entityId);
        service.permanentlyDelete(entityId);
        return new ResponseEntity<>("{\"response\":\"ok\"}", HttpStatus.OK);
    }

//    @GetMapping(path = "getOrderElements", produces = StaticDatas.Produces.JSON)
//    public ResponseEntity<String> getOrderElements(@RequestParam(value = "orderId") Long orderId) throws JsonProcessingException {
//        return new ResponseEntity<>(gson.toJson(service.getOrderElements(orderId)), HttpStatus.OK);
//    }
}

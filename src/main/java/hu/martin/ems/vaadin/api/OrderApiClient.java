package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.Order;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.time.LocalDate;

@Component
public class OrderApiClient extends EmsApiClient<Order> {

    public OrderApiClient() {
        super(Order.class);
    }

    public void createDocumentAsODT(Order o, OutputStream out){
        //TODO
    }

    public byte[] createDocumentAsPDF(Order o, OutputStream out){
        return new byte[0];
        //TODO
    }

    public String generateEmail(Order order){
        //TODO
        return "";
    }

    public void send(LocalDate from, LocalDate to){
        //TODO
    }
}

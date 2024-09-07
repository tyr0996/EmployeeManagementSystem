package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.model.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;

@Component
public class OrderApiClient extends EmsApiClient<Order> {

    public OrderApiClient() {
        super(Order.class);
    }

    public byte[] createDocumentAsODT(Order order, OutputStream out){
        try{
            byte[] response =  webClient.post()
                    .uri("createDocumentAsODT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(order))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            out.write(response);
            return response;
        }
        catch(JsonProcessingException ex){
            logger.error("Error happened while generating ODT document!");
            //TODO
            ex.printStackTrace();
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] createDocumentAsPDF(Order order, OutputStream out){
        try{
            byte[] response =  webClient.post()
                    .uri("createDocumentAsPDF")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(order))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            out.write(response);
            return response;
        }
        catch(JsonProcessingException ex){
            logger.error("Error happened while generating PDF document!");
            //TODO
            ex.printStackTrace();
            return null;
        } catch (IOException e) {
            logger.error("IOException happened while generating PDF document!");
            throw new RuntimeException(e);
        }
    }

    public String generateEmail(Order order){
        //TODO
        return "";
    }

    public void send(LocalDate from, LocalDate to){
        //TODO
    }
}

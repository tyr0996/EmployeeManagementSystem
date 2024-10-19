package hu.martin.ems.vaadin.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.LinkedHashMap;

@Component
@NeedCleanCoding
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
            logger.error("Json processing error happened while sending request for PDF document!");
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
        catch (WebClientResponseException ex) {
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return null;
        }
        catch(JsonProcessingException ex){
            logger.error("Json processing error happened while sending request for PDF document!");
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateEmail(Order order){
        return null; //TODO
    }

    public EmsResponse sendReportSFTPToAccountant(LocalDate from, LocalDate to) {
        LinkedHashMap<String, LocalDate> body = new LinkedHashMap<>();
        body.put("from", from);
        body.put("to", to);
        try{
            String response =  webClient.put()
                    .uri("sendReportSFTPToAccountant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(om.writeValueAsString(body))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, response);
        }
        catch (WebClientResponseException ex) {
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
        catch(JsonProcessingException ex){
            logger.error("Json processing error - Status: {}", 400);
            return new EmsResponse(400, "Data processing error");
        }
    }
}

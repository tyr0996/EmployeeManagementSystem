package hu.martin.ems.vaadin.api;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.LinkedHashMap;

@Component
@NeedCleanCoding
public class OrderApiClient extends EmsApiClient<Order> {

    public OrderApiClient() {
        super(Order.class);
    }

    public EmsResponse createDocumentAsODT(Order order){
        initWebClient();
        try{
            byte[] response =  webClient.post()
                    .uri("createDocumentAsODT")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(gson.toJson(order))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return new EmsResponse(200, new ByteArrayInputStream(response), "");
        }
        catch (WebClientResponseException ex){
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public EmsResponse createDocumentAsPDF(Order order){
        initWebClient();
        try{
            byte[] response =  webClient.post()
                    .uri("createDocumentAsPDF")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(gson.toJson(order))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return new EmsResponse(200, new ByteArrayInputStream(response), "");
        }
        catch (WebClientResponseException ex) {
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }

    public String generateEmail(Order order){
        return null; //TODO
    }

    public EmsResponse sendReportSFTPToAccountant(LocalDate from, LocalDate to) {
        LinkedHashMap<String, LocalDate> body = new LinkedHashMap<>();
        body.put("from", from);
        body.put("to", to);
        initWebClient();
        try{
            String response =  webClient.put()
                    .uri("sendReportSFTPToAccountant")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(gson.toJson(body))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return new EmsResponse(200, response);
        }
        catch (WebClientResponseException ex) {
            logger.error("WebClient error - Status: {}, Body: {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new EmsResponse(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }
    }
}

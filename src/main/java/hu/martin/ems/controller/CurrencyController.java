package hu.martin.ems.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Currency;
import hu.martin.ems.repository.CurrencyRepository;
import hu.martin.ems.service.CurrencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/api/currency")
@AnonymousAllowed
public class CurrencyController extends BaseController<Currency, CurrencyService, CurrencyRepository> {
    public CurrencyController(CurrencyService service) {
        super(service);
    }

    @PutMapping
    public ResponseEntity<String> fetchAndSaveRates() throws JsonProcessingException {
        return new ResponseEntity<>(om.writeValueAsString(service.fetchAndSaveRates()), HttpStatus.OK);
    }


    @GetMapping
    public ResponseEntity<String> findByDate(LocalDate date) throws JsonProcessingException {
        return new ResponseEntity<>(om.writeValueAsString(service.findByDate(date)), HttpStatus.OK);
    }
}

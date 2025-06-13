package hu.martin.ems.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.exception.FetchingCurrenciesException;
import hu.martin.ems.exception.ParsingCurrenciesException;
import hu.martin.ems.model.Currency;
import hu.martin.ems.repository.CurrencyRepository;
import hu.martin.ems.service.CurrencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

@Controller
@RequestMapping("/api/currency")
@AnonymousAllowed
public class CurrencyController extends BaseController<Currency, CurrencyService, CurrencyRepository> {
    public CurrencyController(CurrencyService service) {
        super(service);
    }

    @GetMapping(path = "fetchAndSaveRates", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> fetchAndSaveRates() throws FetchingCurrenciesException, ParsingCurrenciesException {
        Currency c = service.fetchAndSaveRates();
        return new ResponseEntity<>(gson.toJson(c), HttpStatus.OK);
    }


    @GetMapping(path = "findByDate")
    public ResponseEntity<String> findByDate(String date) throws JsonProcessingException {
        LocalDate ld = LocalDate.parse(date);
        return new ResponseEntity<>(gson.toJson(service.findByDate(ld)), HttpStatus.OK);
    }
}

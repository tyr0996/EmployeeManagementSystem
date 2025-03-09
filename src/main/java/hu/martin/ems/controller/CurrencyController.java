package hu.martin.ems.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.exception.FetchingCurrenciesException;
import hu.martin.ems.exception.ParsingCurrenciesException;
import hu.martin.ems.model.Currency;
import hu.martin.ems.repository.CurrencyRepository;
import hu.martin.ems.service.CurrencyService;
import hu.martin.ems.vaadin.api.Error;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    @GetMapping(path = "fetchAndSaveRates", produces = StaticDatas.Produces.JSON)
    public ResponseEntity<String> fetchAndSaveRates() throws FetchingCurrenciesException, ParsingCurrenciesException {
        Currency c = service.fetchAndSaveRates();
        return new ResponseEntity<>(gson.toJson(c), HttpStatus.OK);
    }


    @GetMapping(path = "findByDate")
    public ResponseEntity<String> findByDate(String date) throws JsonProcessingException {
        LocalDate ld = LocalDate.parse(date);
        return new ResponseEntity<>(gson.toJson(service.findByDate(ld)), HttpStatus.OK);
    }

    @ExceptionHandler(FetchingCurrenciesException.class)
    public ResponseEntity<String> handleFetchingCurrenciesException(FetchingCurrenciesException ex, HttpServletRequest request) {
        Error errorResponse = new Error(System.currentTimeMillis(), 502, ex.getType().getText(), request.getRequestURI());
        return new ResponseEntity<>(gson.toJson(errorResponse), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(ParsingCurrenciesException.class)
    public ResponseEntity<String> handleParsingCurrenciesException(ParsingCurrenciesException ex, HttpServletRequest request) {
        Error errorResponse = new Error(System.currentTimeMillis(), 500, ex.getType().getText(), request.getRequestURI());
        return new ResponseEntity<>(gson.toJson(errorResponse), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

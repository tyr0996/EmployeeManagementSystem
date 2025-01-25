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

    @GetMapping(path = "fetchAndSaveRates")
    public ResponseEntity<String> fetchAndSaveRates() throws JsonProcessingException {
        try{
            Currency c = service.fetchAndSaveRates();
            return new ResponseEntity<>(gson.toJson(c), HttpStatus.OK);
        }
        catch (FetchingCurrenciesException ex) {
            return new ResponseEntity<>(ex.getType().getText(), HttpStatus.BAD_GATEWAY);
        }
        catch (ParsingCurrenciesException ex) {
            return new ResponseEntity<>(ex.getType().getText(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(path = "findByDate")
    public ResponseEntity<String> findByDate(String date) throws JsonProcessingException {
        String[] d = date.split("-");
        LocalDate ld = LocalDate.of(Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2])); //TODO megformázni a dateformatterrel vagy mivel
        return new ResponseEntity<>(gson.toJson(service.findByDate(ld)), HttpStatus.OK);
    }
}

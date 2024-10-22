package hu.martin.ems.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.config.StaticDatas;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.model.Currency;
import hu.martin.ems.repository.CurrencyRepository;
import hu.martin.ems.service.CurrencyService;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        EmsResponse emsResponse = service.fetchAndSaveRates();
        int code = emsResponse.getCode();
        if(code == 200){
            return new ResponseEntity<>(om.writeValueAsString(emsResponse), HttpStatus.valueOf(emsResponse.getCode()));
        }
        else {
            return new ResponseEntity<>(emsResponse.getDescription(), HttpStatusCode.valueOf(emsResponse.getCode()));
        }
    }


    @GetMapping(path = "findByDate")
    public ResponseEntity<String> findByDate(String date) throws JsonProcessingException {
        String[] d = date.split("-");
        LocalDate ld = LocalDate.of(Integer.parseInt(d[0]), Integer.parseInt(d[1]), Integer.parseInt(d[2])); //TODO megformázni a dateformatterrel vagy mivel
        return new ResponseEntity<>(om.writeValueAsString(service.findByDate(ld)), HttpStatus.OK);
    }
}

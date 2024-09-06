package hu.martin.ems.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.Currency;
import hu.martin.ems.repository.CurrencyRepository;
import hu.martin.ems.service.CurrencyService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/currency")
@AnonymousAllowed
public class CurrencyController extends BaseController<Currency, CurrencyService, CurrencyRepository> {
    public CurrencyController(CurrencyService service) {
        super(service);
    }
}

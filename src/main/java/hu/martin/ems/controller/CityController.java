package hu.martin.ems.controller;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.controller.BaseController;
import hu.martin.ems.model.City;
import hu.martin.ems.repository.CityRepository;
import hu.martin.ems.service.CityService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/city")
@AnonymousAllowed
@NeedCleanCoding
public class CityController extends BaseController<City, CityService, CityRepository> {

    public CityController(CityService service) {
        super(service);
    }
}

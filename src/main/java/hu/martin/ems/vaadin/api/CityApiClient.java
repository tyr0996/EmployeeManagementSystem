package hu.martin.ems.vaadin.api;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.model.City;
import org.springframework.stereotype.Component;

@Component
@NeedCleanCoding
public class CityApiClient extends EmsApiClient<City> {
    public CityApiClient() {
        super(City.class);
    }
}

package hu.martin.ems.vaadin.api;

import hu.martin.ems.model.City;
import org.springframework.stereotype.Component;

@Component
public class CityApiClient extends EmsApiClient<City> {
    public CityApiClient() {
        super(City.class);
    }
}

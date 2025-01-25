package hu.martin.ems.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.City;
import hu.martin.ems.repository.CityRepository;
import org.springframework.stereotype.Service;

@Service
@NeedCleanCoding
public class CityService extends BaseService<City, CityRepository> {
    public CityService(CityRepository cityRepository) {
        super(cityRepository);
    }
}

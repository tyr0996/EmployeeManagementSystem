package hu.martin.ems.service;

import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.City;
import hu.martin.ems.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CityService extends BaseService<City, CityRepository>{
    public CityService(CityRepository cityRepository) { super(cityRepository); }
}

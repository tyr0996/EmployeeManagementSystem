package hu.martin.ems.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.core.apiresponse.CurrencyAPI;
import hu.martin.ems.core.apiresponse.CurrencyResponse;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Currency;
import hu.martin.ems.repository.CodeStoreRepository;
import hu.martin.ems.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.LinkedHashMap;

@Service
@Transactional
public class CurrencyService extends BaseService<Currency, CurrencyRepository> {
    public CurrencyService(CurrencyRepository currencyRepository,
                           CodeStoreRepository codeStoreRepository,
                           RestTemplate restTemplate) {
        super(currencyRepository);
        this.restTemplate = restTemplate;
        this.codeStoreRepository = codeStoreRepository;
    }

    private final ObjectMapper om = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final CodeStoreRepository codeStoreRepository;

    @Value("${api.currency.url}")
    private String apiUrl;

    @Value("${api.currency.baseCurrency}")
    private String baseCurrency;

    public Currency fetchAndSaveRates() {
        Currency curr = this.repo.findByDate(LocalDate.now());
        if(curr != null){
            return curr;
        }
        try{
            Object response = restTemplate.getForObject(apiUrl + baseCurrency, Object.class);
            String json = om.writeValueAsString(response);
            CurrencyAPI c = om.readValue(json, CurrencyAPI.class);
            LinkedHashMap<String, Double> map = om.convertValue(c.getRates(), LinkedHashMap.class);
            map.forEach((k, v) -> map.replace(k, Double.valueOf(new DecimalFormat("#0.000").format((1.0 / v)).replace(",", "."))));
            c.setRates(om.convertValue(map, CurrencyResponse.class));

            Currency currency = new Currency();
            currency.setBaseCurrency(codeStoreRepository.findByName(c.getBase()));
            currency.setRateJson(om.writeValueAsString(c.getRates()));
            currency.setValidDate(c.getValidDate());
            currency.setDeleted(0L);
            return this.repo.customSave(currency);
        } catch (RestClientException e){
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Double convert(LocalDate date, String from, String to, Double amount) {
        Currency c = this.repo.findByDate(date);
        if (c != null) {
            LinkedHashMap<String, Double> map;
            try {
                map = om.readValue(c.getRateJson(), LinkedHashMap.class);
                Double f = map.get(from.toUpperCase());
                Double t = map.get(to.toUpperCase());
                return (f * amount) / t;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else if (date.equals(LocalDate.now())) {
            fetchAndSaveRates();
            return convert(date, from, to, amount);
        } else {
            throw new NullPointerException("Nincs árfolyam az adott dátumhoz elmentve, és annak lekérdezése csak a mai napra lehetséges!");
        }
    }

    public Double get(LocalDate date, String currency) {
        Currency c = this.repo.findByDate(date);
        if (c != null) {
            LinkedHashMap<String, Double> map = null;
            try {
                map = om.readValue(c.getRateJson(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return map.get(currency);
        } else {
            throw new NullPointerException("Nincs árfolyam az adott dátumhoz elmentve!");
        }
    }

    public Currency findByDate(LocalDate now) {
        return this.repo.findByDate(now);
    }
}

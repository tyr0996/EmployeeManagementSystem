package hu.martin.ems.service;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.apiresponse.CurrencyResponse;
import hu.martin.ems.core.config.JacksonConfig;
import hu.martin.ems.core.model.EmsResponse;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.model.Currency;
import hu.martin.ems.repository.CodeStoreRepository;
import hu.martin.ems.repository.CurrencyRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.LinkedHashMap;

@Service
@Transactional
@NeedCleanCoding
public class CurrencyService extends BaseService<Currency, CurrencyRepository> {
    public CurrencyService(CurrencyRepository currencyRepository) {
        super(currencyRepository);
    }

    private ObjectMapper om;

    @Autowired
    @Setter
    @Getter
    private RestTemplate restTemplate;

    @Autowired
    @Getter
    @Setter
    private CodeStoreRepository codeStoreRepository;

    @Value("${api.currency.url}")
    @Setter
    private String apiUrl;

    @Setter
    @Value("${api.currency.baseCurrency}")
    private String baseCurrency;

    public EmsResponse fetchAndSaveRates() {
        om = new JacksonConfig().objectMapper();
        Currency curr = repo.findByDate(LocalDate.now());
        if(curr != null){
            return new EmsResponse(200, curr, "");
        }
        try {
            LinkedHashMap<String, Object> response = restTemplate.getForObject(apiUrl + baseCurrency, LinkedHashMap.class);
            LinkedHashMap<String, Object> rates = (LinkedHashMap<String, Object>) response.get("rates");
            rates.forEach((k, v) -> {
                rates.replace(k, Double.parseDouble(v.toString()));
            });
            String fixedRates = response.get("rates").toString().replaceAll("\\b[A-Z]+\\b", "\"$0\"") //Belerakja idézőjelbe
                    .replaceAll("=", ":");
            Currency currency = new Currency();
            currency.setBaseCurrency(codeStoreRepository.findByName(response.get("base").toString()));
            //CurrencyResponse cr = om.readValue(fixedRates, CurrencyResponse.class);
            currency.setRateJson(fixedRates);

            currency.setValidDate(getValidDate(response.get("date").toString()));
            currency.setDeleted(0L);
            Object saved = this.repo.customSave(currency);
            return new EmsResponse(200, saved, "");
        } catch (ClassCastException e){
            return new EmsResponse(500, EmsResponse.Description.PARSING_CURRENCIES_FAILED);
        } catch (RestClientException e){
            return new EmsResponse(500, EmsResponse.Description.FETCHING_CURRENCIES_FAILED);
        }
    }

    public LocalDate getValidDate(String lastUpdated) {
        String[] date = lastUpdated.split("-");
        LocalDate ld = LocalDate.of(Integer.parseInt(date[0]),
                Integer.parseInt(date[1]),
                Integer.parseInt(date[2]));
        return ld;
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
        om = new JacksonConfig().objectMapper();
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

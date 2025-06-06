package hu.martin.ems.service;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.service.BaseService;
import hu.martin.ems.exception.CurrencyException;
import hu.martin.ems.exception.FetchingCurrenciesException;
import hu.martin.ems.exception.ParsingCurrenciesException;
import hu.martin.ems.model.Currency;
import hu.martin.ems.repository.CodeStoreRepository;
import hu.martin.ems.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.LinkedHashMap;

@Service
@NeedCleanCoding
public class CurrencyService extends BaseService<Currency, CurrencyRepository> {
    public CurrencyService(CurrencyRepository currencyRepository) {
        super(currencyRepository);
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CodeStoreRepository codeStoreRepository;

    @Value("${api.currency.url}")
    private String apiUrl;

    @Value("${api.currency.baseCurrency}")
    private String baseCurrency;

    @Autowired
    private Gson gson;

    public Currency fetchAndSaveRates() throws ParsingCurrenciesException, FetchingCurrenciesException {
        try {
            LinkedHashMap<String, Object> response = restTemplate.getForObject(apiUrl + baseCurrency, LinkedHashMap.class);
            LinkedHashMap<String, Object> rates = (LinkedHashMap<String, Object>) response.get("rates");
            rates.forEach((k, v) -> {
                rates.replace(k, Double.parseDouble(v.toString()));
            });
            String fixedRates = response.get("rates").toString().replaceAll("\\b[A-Z]+\\b", "\"$0\"")
                    .replaceAll("=", ":");
            Currency currency = new Currency();

            currency.setBaseCurrency(codeStoreRepository.findByName(response.get("base").toString()));
            currency.setRateJson(fixedRates);
            currency.setDeleted(0L);
            Currency saved = this.repo.customSave(currency);
            return saved;
        } catch (ClassCastException e) {
            throw new ParsingCurrenciesException();
        } catch (RestClientException e) {
            throw new FetchingCurrenciesException();
        }
    }

    public Double convert(String from, String to, Double amount) throws CurrencyException {
        Currency c = this.repo.findByDate(LocalDate.now());
        if (c != null) {
            LinkedTreeMap<String, Double> map = gson.fromJson(c.getRateJson(), LinkedTreeMap.class);
            Double f = map.get(from.toUpperCase());
            Double t = map.get(to.toUpperCase());
            return (f * amount) / t;
        } else {
            fetchAndSaveRates();
            return convert(from, to, amount);
        }
    }

    public Currency findByDate(LocalDate date) {
        return this.repo.findByDate(date);
    }
}

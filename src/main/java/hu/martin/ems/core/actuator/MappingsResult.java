package hu.martin.ems.core.actuator;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.internal.LinkedTreeMap;
import hu.martin.ems.core.config.BeanProvider;
import org.springframework.http.HttpMethod;

import java.util.LinkedHashMap;
import java.util.List;

public class MappingsResult {
    @Expose
    List<EndpointDetail> endpointDetails;
    public MappingsResult(String mappingsEndpointResultJson){
        Gson gson = BeanProvider.getBean(Gson.class);
        LinkedHashMap<String, Object> map = gson.fromJson(mappingsEndpointResultJson, LinkedHashMap.class);
        LinkedTreeMap<String, Object> contexts = (LinkedTreeMap<String, Object>) map.get("contexts");
        LinkedTreeMap<String, Object> ems = (LinkedTreeMap<String, Object>) contexts.get("ems");
        LinkedTreeMap<String, Object> mappings = (LinkedTreeMap<String, Object>) ems.get("mappings");
        LinkedTreeMap<String, Object> dispatcherServlets = (LinkedTreeMap<String, Object>) mappings.get("dispatcherServlets");
        List<LinkedTreeMap<String, Object>> dispatcherServlet = (List<LinkedTreeMap<String, Object>>) dispatcherServlets.get("dispatcherServlet");
        this.endpointDetails = dispatcherServlet.stream()
                .map(entry -> ((String) entry.get("predicate")).split(", produces")[0])
                .map(row -> row.substring(1, row.length() - 1))
                .filter(trimmedRow -> !trimmedRow.equals("*"))
                .map(trimmedRow -> {
                    String[] parts = trimmedRow.split("\\[");
                    return new EndpointDetail(parts[1], HttpMethod.valueOf(parts[0]));
                }).toList();
    }
}

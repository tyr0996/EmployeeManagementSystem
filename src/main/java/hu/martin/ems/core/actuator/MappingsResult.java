package hu.martin.ems.core.actuator;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.internal.LinkedTreeMap;
import hu.martin.ems.core.config.BeanProvider;

import java.util.ArrayList;
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
        this.endpointDetails = new ArrayList<>();
        for(int i = 0; i < dispatcherServlet.size(); i++){
            String row = ((String) dispatcherServlet.get(i).get("predicate")).split(", produces")[0];
            String trimmedRow = row.substring(1, row.length() - 1);
            System.out.println("Trimmed row: " + trimmedRow);
            if(!trimmedRow.equals("*")){ //TODO ezt az esetet egy kicsit jobban meg kellene oldani (tehát már korábban kiszűrni)
                this.endpointDetails.add(new EndpointDetail(
                        trimmedRow.split("\\[")[0],
                        trimmedRow.split("\\[")[1])
                );
            }
        }
    }
}

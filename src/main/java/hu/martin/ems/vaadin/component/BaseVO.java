package hu.martin.ems.vaadin.component;

import com.vaadin.ui.Link;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public abstract class BaseVO {
    public Long id;
    @NotNull
    public Long deleted;
    public static LinkedHashMap<String, List<String>> showDeletedCheckboxFilter = new LinkedHashMap<>();
    public static LinkedHashMap<String, List<String>> extraDataFilterMap = new LinkedHashMap<>();

    public BaseVO(Long id, Long deleted){
        this.id = id;
        this.deleted = deleted;
    }

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseVO that = (BaseVO) o;
        return Objects.equals(this.id, that.id);
    }

    public boolean filterExtraData() {
        LinkedHashMap<String, List<String>> merged = mergeMaps(showDeletedCheckboxFilter, extraDataFilterMap);

        Boolean filteredResult = true;
        String[] keys = merged.keySet().toArray(new String[0]);
        for(int i = 0; i < merged.size(); i++){
//            Ki kellett kommentelnem az if-t. Az lenne a lényeg, hogyha majd új filterek jönnek be, akkor
//            if(keys[i].equals("deleted")){
                Boolean matchingDeleted = merged.get(keys[i]).contains(this.deleted.toString());
                if(!matchingDeleted){
                    return false;
                }
                else{}
//            }
//            else{}
        }
        return filteredResult;
    }

    private LinkedHashMap<String, List<String>> mergeMaps(LinkedHashMap<String, List<String>> map1, LinkedHashMap<String, List<String>> map2) {
        LinkedHashMap<String, List<String>> resultMap = new LinkedHashMap<>();

        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(map1.keySet());
        allKeys.addAll(map2.keySet());

        for (String key : allKeys) {
            List<String> mergedValues = new ArrayList<>();

            if (map1.containsKey(key) && map2.containsKey(key)) {
                List<String> list1 = map1.get(key);
                List<String> list2 = map2.get(key);
                mergedValues.addAll(getIntersection(list1, list2));
            }
            else if (map1.containsKey(key)) {
                mergedValues.addAll(map1.get(key));
            }
            else {
                mergedValues.addAll(map2.get(key));
            }

            resultMap.put(key, mergedValues);
        }

        return resultMap;
    }

    private List<String> getIntersection(List<String> list1, List<String> list2) {
        List<String> intersection = new ArrayList<>(list1);
        intersection.retainAll(list2);
        return intersection;
    }
}

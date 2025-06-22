package hu.martin.ems.core.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import hu.martin.ems.vaadin.component.BaseVO;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.*;

public interface IEmsFilterableGridPage<T extends BaseVO> {
    PaginatedGrid<T, String> getGrid();

    void updateGridItems();

    default Component styleFilterField(Component filterField, String title) {
        VerticalLayout res = new VerticalLayout();
        res.getStyle().set("padding", "0px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center");
        filterField.getStyle().set("display", "flex").set("width", "100%");
        NativeLabel titleLabel = new NativeLabel(title);
        res.add(titleLabel, filterField);
        res.setClassName("vaadin-header-cell-content");
        return res;
    }

    default boolean filterFieldWithNullFilter(TextFilteringHeaderCell filterField, String fieldValue) {
        if (filterField.getFilterText().toLowerCase().equals("null")) {
            return fieldValue.isEmpty();
        } else {
            return filterField(filterField, fieldValue);
        }
    }

    default boolean filterField(TextFilteringHeaderCell filterField, String fieldValue) {
        return filterField.isEmpty() || fieldValue.toLowerCase().contains(filterField.getFilterText().toLowerCase());
    }

    default boolean filterField(NumberFilteringHeaderCell filterField, Double fieldValue){
        return filterField.isEmpty() || fieldValue.equals(filterField.getFilterNumber());
    }

    default boolean filterExtraData(ExtraDataFilterField extraDataFilterField, T entity, LinkedHashMap<String, List<String>> showDeleted){
        LinkedHashMap<String, List<String>> merged = mergeMaps(extraDataFilterField.getExtraDataFilterValue(), showDeleted);

        Boolean filteredResult = true;
        String[] keys = merged.keySet().toArray(new String[0]);
        for (int i = 0; i < merged.size(); i++) {
//            Ki kellett kommentelnem az if-t. Az lenne a lényeg, hogyha majd új filterek jönnek be, akkor
//            if(keys[i].equals("deleted")){
            Boolean matchingDeleted = merged.get(keys[i]).contains(entity.deleted.toString());
            if (!matchingDeleted) {
                return false;
            } else {}
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
            } else if (map1.containsKey(key)) {
                mergedValues.addAll(map1.get(key));
            } else {
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

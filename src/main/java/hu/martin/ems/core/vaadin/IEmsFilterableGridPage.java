package hu.martin.ems.core.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.klaudeta.PaginatedGrid;

public interface IEmsFilterableGridPage<T> {
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
}

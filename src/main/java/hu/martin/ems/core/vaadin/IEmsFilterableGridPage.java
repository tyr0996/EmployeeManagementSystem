package hu.martin.ems.core.vaadin;

import org.vaadin.klaudeta.PaginatedGrid;

public interface IEmsFilterableGridPage<T> {
    PaginatedGrid<T, String> getGrid();
    void updateGridItems();
}

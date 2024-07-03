package hu.martin.ems.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.vaadin.klaudeta.PaginatedGrid.PaginationLocation;

@Getter
@AllArgsConstructor
public class PaginationSetting {
    private PaginationLocation paginationLocation;
    private Integer pageSize;
}

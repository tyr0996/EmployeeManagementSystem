package hu.martin.ems.core.model;

import hu.martin.ems.NeedCleanCoding;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.vaadin.klaudeta.PaginatedGrid.PaginationLocation;

@Getter
@AllArgsConstructor
@NeedCleanCoding
public class PaginationSetting {
    private PaginationLocation paginationLocation;
    private Integer pageSize;
}

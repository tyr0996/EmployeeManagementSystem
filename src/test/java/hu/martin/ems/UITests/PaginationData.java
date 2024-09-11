package hu.martin.ems.UITests;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaginationData {
    private Integer pageSize;
    private Integer totalElements;
    private Integer currentPage;
}

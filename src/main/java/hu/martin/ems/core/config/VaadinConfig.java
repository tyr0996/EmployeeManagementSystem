package hu.martin.ems.core.config;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.PaginationSetting;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vaadin.klaudeta.PaginatedGrid.PaginationLocation;

@Configuration
@NeedCleanCoding
public class VaadinConfig {


    @Value("${vaadin.pagination.pageSize}")
    private Integer pageSize;

    @Value("${vaadin.pagination.paginationPosition}")
    private String paginationPosition;

    @Bean
    public PaginationSetting paginationSetting() {
        return new PaginationSetting(PaginationLocation.valueOf(paginationPosition), pageSize);
    }
}

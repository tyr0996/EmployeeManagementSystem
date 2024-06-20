package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Currency extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "baseCurrency_codestore_id")
    private CodeStore baseCurrency;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rateJson;

    private LocalDate validDate;

    @Transient
    private String name;

    public String getName() {
        return baseCurrency.getName() + " (" + validDate + ")";
    }

}

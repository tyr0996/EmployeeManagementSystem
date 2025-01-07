package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NeedCleanCoding
public class Currency extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "baseCurrency_codestore_id")
    @Expose
    private CodeStore baseCurrency;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Expose
    private String rateJson;

    @Expose
    private LocalDate validDate;

    @Transient
    @Expose
    private String name;

    public String getName() {
        return baseCurrency.getName() + " (" + validDate + ")";
    }

}

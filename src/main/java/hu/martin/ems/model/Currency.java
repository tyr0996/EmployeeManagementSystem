package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

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
}

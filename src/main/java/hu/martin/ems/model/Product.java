package hu.martin.ems.model;

import hu.martin.ems.NeedCleanCoding;
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
public class Product extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double buyingPriceNet;

    @ManyToOne
    @JoinColumn(name = "buyingPriceCurrency_codestore_id", nullable = false)
    private CodeStore buyingPriceCurrency;

    @Column(nullable = false)
    private Double sellingPriceNet;

    @ManyToOne
    @JoinColumn(name = "sellingPriceCurrency_codestore_id", nullable = false)
    private CodeStore sellingPriceCurrency;

    @ManyToOne
    @JoinColumn(name = "amountUnit_codestore_id", nullable = false)
    private CodeStore amountUnit;

    @Column(nullable = false)
    private Double amount;

    @ManyToOne
    @JoinColumn(name = "taxKey_codestore_id", nullable = false)
    private CodeStore taxKey;


}

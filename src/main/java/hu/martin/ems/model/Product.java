package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.vaadin.core.NamedObject;
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
public class Product extends BaseEntity implements NamedObject {
    @Column(nullable = false)
    @Expose
    private String name;

    @Column(nullable = false)
    @Expose
    private Double buyingPriceNet;

    @ManyToOne
    @JoinColumn(name = "buyingPriceCurrency_codestore_id", nullable = false)
    @Expose
    private CodeStore buyingPriceCurrency;

    @Column(nullable = false)
    @Expose
    private Double sellingPriceNet;

    @ManyToOne
    @JoinColumn(name = "sellingPriceCurrency_codestore_id", nullable = false)
    @Expose
    private CodeStore sellingPriceCurrency;

    @ManyToOne
    @JoinColumn(name = "packingUnit_codestore_id", nullable = false)
    @Expose
    private CodeStore packingUnit;

    @Column(nullable = false)
    @Expose
    private Double amount;

    @ManyToOne
    @JoinColumn(name = "taxKey_codestore_id", nullable = false)
    @Expose
    private CodeStore taxKey;


}

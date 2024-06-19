package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class OrderElement extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "product_product_id")
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "order_order_id", columnDefinition = "BIGINT")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "customer_customer_id", columnDefinition = "BIGINT")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "supplier_supplier_id", columnDefinition = "BIGINT")
    private Supplier supplier;

    @Column(nullable = false)
    private Integer unit;
    
    @Column(nullable = false)
    private Integer unitNetPrice;
    
    @ManyToOne
    @JoinColumn(name = "taxKey_codestore_id")
    private CodeStore taxKey;
    
    @Column(nullable = false)
    private Integer netPrice;

    @Column(nullable = false)
    private Double taxPrice;
    
    @Column(nullable = false)
    private Integer grossPrice;
    
    @Transient
    private String name;
    
    public String getName() {
        return this.product.getName();
    }
    
}

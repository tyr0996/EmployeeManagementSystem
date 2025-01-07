package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NeedCleanCoding
public class OrderElement extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "product_product_id")
    @Expose
    private Product product;

    @ManyToOne
    @JoinTable(name = "orders_orderelement",
        joinColumns = @JoinColumn(name = "orderelements_id"),
        inverseJoinColumns = @JoinColumn(name = "order_id")
    )
//    @JoinColumn(name = "order_id", columnDefinition = "BIGINT")
    private Order orderObject;


    @ManyToOne
    @JoinColumn(name = "customer_customer_id", columnDefinition = "BIGINT")
    @Expose
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "supplier_supplier_id", columnDefinition = "BIGINT")
    @Expose
    private Supplier supplier;

    @Column(nullable = false)
    @Expose
    private Integer unit;

    @Column(nullable = false)
    @Expose
    private Integer unitNetPrice;

    @ManyToOne
    @JoinColumn(name = "taxKey_codestore_id")
    @Expose
    private CodeStore taxKey;

    @Column(nullable = false)
    @Expose
    private Integer netPrice;

    @Column(nullable = false)
    @Expose
    private Double taxPrice;

    @Column(nullable = false)
    @Expose
    private Integer grossPrice;

    @Expose
    @Transient
    private String name;

    @Expose
    @Transient
    private Long orderId;

    @PostLoad
    public void initAfterLoad(){
        if(orderObject != null){
            orderId = orderObject.getId();
        }
    }


    public String getName() {
        return this.product.getName();
    }

    @Override
    public String toString(){
        return this.getName() + " (" + this.unit + " " + this.product.getAmountUnit().getName() + ")";
    }

}

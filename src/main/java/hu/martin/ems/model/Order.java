package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Order extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "state_codestore_id")
    private CodeStore state;
    
    @Column(nullable = false)
    private LocalDateTime timeOfOrder;
    
    @ManyToOne
    @JoinColumn(name = "customer_customer_id", columnDefinition = "BIGINT")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "supplier_supplier_id", columnDefinition = "BIGINT")
    private Supplier supplier;
    
    @ManyToOne
    @JoinColumn(name = "paymentType_codestore_id")
    private CodeStore paymentType;
    
    @Transient
    private String name;
    
    public String getName() {
        return this.getId() + " (" + this.state.getName() + ")";
    }
    
}

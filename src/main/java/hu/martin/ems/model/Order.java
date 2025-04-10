package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
@NeedCleanCoding
public class Order extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "state_codestore_id")
    @Expose
    private CodeStore state;

    @Column(nullable = false)
    @Expose
    private LocalDateTime timeOfOrder;

    @ManyToOne
    @JoinColumn(name = "customer_customer_id", columnDefinition = "BIGINT")
    @Expose
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "supplier_supplier_id", columnDefinition = "BIGINT")
    @Expose
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "paymentType_codestore_id")
    @Expose
    private CodeStore paymentType;

    @ManyToOne
    @JoinColumn(name = "currency_codestore_id")
    @Expose
    private CodeStore currency;

    @OneToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.EAGER)
    @Expose
//    @JoinColumn(name = "order_order_id")
    private List<OrderElement> orderElements;

    @Transient
    @Expose
    private String name;

    public String getName() {
        return this.getId() + " (" + this.state.getName() + ")";
    }

    @AssertTrue(message = "At least one of the fields (field1 or field2) must have a value.")
    public boolean isValid() {
        System.out.println("Order is valid: " + (customer != null) + "    " + (supplier != null));
        return customer != null || supplier != null;
    }
}

package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Supplier extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "address_address_id")
    private Address address;
}
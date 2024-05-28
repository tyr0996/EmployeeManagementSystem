package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

@Entity
@Getter
@Setter
public class Customer extends BaseEntity {
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @ManyToOne
    @JoinColumn(name = "address_address_id")
    private Address address;
    
    @Transient
    private String name;
    
    public String getName() {
        return firstName + " " + lastName;
    }
    
}

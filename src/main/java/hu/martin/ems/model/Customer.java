package hu.martin.ems.model;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NeedCleanCoding
public class Customer extends BaseEntity {
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "address_address_id")
    private Address address;

    @Column(nullable = false)
    private String emailAddress;

    @Transient
    private String name;

    public String getName() {
        return firstName + " " + lastName;
    }

}

package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.vaadin.core.NamedObject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NeedCleanCoding
public class Customer extends BaseEntity implements NamedObject {
    @Column(nullable = false)
    @Expose
    private String firstName;

    @Column(nullable = false)
    @Expose
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "address_address_id")
    @Expose
    private Address address;

    @Column(nullable = false)
    @Expose
    private String emailAddress;

    @Transient
    @Expose
    private String name;

    public String getName() {
        return firstName + " " + lastName;
    }

}

package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Address extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "countryCode_codestore_id")
    private CodeStore countryCode;

    @ManyToOne
    @JoinColumn(name = "city_city_id")
    private City city;

    @Column(nullable = false)
    private String streetName;

    @Column(nullable = false)
    private String houseNumber;

    @ManyToOne
    @JoinColumn(name = "streetType_codestore_id")
    private CodeStore streetType;

    @Transient
    private String name;

    public String getName() {
        return countryCode.getName() + " " + city.getZipCode() + " " + city.getName() + " " + streetName + " " + houseNumber;
    }

}

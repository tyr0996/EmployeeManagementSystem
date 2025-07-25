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
public class Address extends BaseEntity implements NamedObject {
    @ManyToOne
    @JoinColumn(name = "countryCode_codestore_id")
    @Expose
    private CodeStore countryCode;

    @ManyToOne
    @JoinColumn(name = "city_city_id")
    @Expose
    private City city;

    @Column(nullable = false)
    @Expose
    private String streetName;

    @Column(nullable = false)
    @Expose
    private String houseNumber;

    @ManyToOne
    @JoinColumn(name = "streetType_codestore_id")
    @Expose
    private CodeStore streetType;

    @Transient
    @Expose
    private String name;

    public String getName() {
        return countryCode.getName() + " " + city.getZipCode() + " " + city.getName() + " " + streetName + " " + houseNumber;
    }

}

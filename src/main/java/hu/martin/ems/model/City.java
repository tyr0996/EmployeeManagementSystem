package hu.martin.ems.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NeedCleanCoding
public class City extends BaseEntity {
    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "countryCode_codestore_id")
    //@JsonIgnore
    private CodeStore countryCode;

    @Column(nullable = false)
    private String zipCode;

}

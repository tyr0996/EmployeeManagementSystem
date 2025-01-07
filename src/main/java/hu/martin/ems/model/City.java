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
public class City extends BaseEntity {
    @Column(nullable = false)
    @Expose
    private String name;

    @ManyToOne
    @JoinColumn(name = "countryCode_codestore_id")
    @Expose
    //@JsonIgnore
    private CodeStore countryCode;

    @Column(nullable = false)
    @Expose
    private String zipCode;

}

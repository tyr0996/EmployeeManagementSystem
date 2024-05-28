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
public class City extends BaseEntity {
    @Column(nullable = false)
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "countryCode_codestore_id")
    private CodeStore countryCode;
    
    @Column(nullable = false)
    private String zipCode;
    
}

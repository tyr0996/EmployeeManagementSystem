package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.vaadin.core.NamedObject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NeedCleanCoding
public class City extends BaseEntity implements NamedObject {
    @Column(nullable = false)
    @Expose
    private String name;

    @ManyToOne
    @JoinColumn(name = "countryCode_codestore_id")
    @Expose
    private CodeStore countryCode;

    @Column(nullable = false)
    @Expose
    private String zipCode;
}

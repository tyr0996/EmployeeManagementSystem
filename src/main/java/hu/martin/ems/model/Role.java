package hu.martin.ems.model;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NeedCleanCoding
public class Role extends BaseEntity {
    @Column(nullable = false)
    private String name;
}

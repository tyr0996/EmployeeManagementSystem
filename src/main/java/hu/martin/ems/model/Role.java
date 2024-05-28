package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Role extends BaseEntity {
    @Column(nullable = false)
    private String name;
}

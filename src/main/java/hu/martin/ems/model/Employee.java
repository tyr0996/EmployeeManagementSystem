package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Employee extends BaseEntity {

    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @ManyToOne
    @JoinColumn(name = "role_role_id")
    private Role role;
    @Column(nullable = false)
    private Integer salary;

    @Transient
    private String name;

    public String getName() {
        return this.firstName + " " + this.lastName;
    }
}

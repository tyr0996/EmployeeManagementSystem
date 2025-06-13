package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.core.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@NeedCleanCoding
public class Employee extends BaseEntity {

    @Column(nullable = false)
    @Expose
    private String firstName;

    @Column(nullable = false)
    @Expose
    private String lastName;

    @ManyToOne
    @JoinColumn(name = "user_loginuser_id")
    @Expose
    private User user;

    @Column(nullable = false)
    @Expose
    private Integer salary;
}

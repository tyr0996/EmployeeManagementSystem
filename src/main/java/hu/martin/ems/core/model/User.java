package hu.martin.ems.core.model;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.model.Role;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Table(name = "loginuser")
@NeedCleanCoding
@NoArgsConstructor
@Setter
public class User extends BaseEntity{
    private String username;

    private String password;

    @ManyToOne
    @JoinColumn(name = "role_role_id", columnDefinition = "BIGINT")
    private Role roleRole;
}

package hu.martin.ems.core.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.model.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Table(name = "loginuser")
@NeedCleanCoding
@NoArgsConstructor
@Setter
public class User extends BaseEntity{
    @Expose
    private String username;

    @Expose
    private String password;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
//    @JoinColumn(name = "role_role_id", columnDefinition = "BIGINT")
    @JoinColumn(name = "role_role_id")
    @Expose
    private Role roleRole;
}

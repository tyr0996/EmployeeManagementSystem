package hu.martin.ems.core.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.vaadin.core.NamedObject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Table(name = "loginuser")
@NeedCleanCoding
@NoArgsConstructor
@Setter
public class User extends BaseEntity implements NamedObject {
    @Expose
    private String username;

    @Expose
    private String passwordHash;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinColumn(name = "role_role_id")
    @Expose
    private Role roleRole;

    @Transient
    @Expose
    private String roles;

    public String getRoles() {
        List<Permission> permissions = this.roleRole.getPermissions().stream().toList();
        String[] ret = new String[permissions.size() + 1];
        ret[0] = this.roleRole.getName();
        for (int i = 1; i < permissions.size() + 1; i++) {
            ret[i] = permissions.get(i - 1).getName();
        }
        return String.join(",", ret);
    }

    @Expose
    private boolean enabled;

    public String getName(){
        return getUsername();
    }
}

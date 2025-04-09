package hu.martin.ems.model;


import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Entity
@Getter
@Setter
@NeedCleanCoding
@AllArgsConstructor
@NoArgsConstructor
public class Permission extends BaseEntity {
    @Column(nullable = false)
    @Expose
    private String name;

    @ManyToMany
//    @JoinTable(name = "roles_permissions",
//            joinColumns = { @JoinColumn(name = "permission_id") },
//            inverseJoinColumns = { @JoinColumn(name = "role_id") }
//    )
    @Expose
    private Set<Role> roles;

//    @Expose
//    @Transient
//    List<Long> roleIds = new ArrayList<>();

    @PostLoad
    public void initAfterLoad(){
        roles.forEach(v -> v.setPermissions(null));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name + id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(name, that.name) && Objects.equals(id, that.id);
    }
}

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
@EqualsAndHashCode //Az equals-ot nem használjuk
@NeedCleanCoding
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(
        name = "Permission",
        attributeNodes = {
                @NamedAttributeNode("roles")
        }
)
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
        if(roles != null){
            roles.forEach(v -> v.setPermissions(null));
//        if(roles != null){
//            roleIds = roles.stream().map(Role::getId).toList();
//        }
        }

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

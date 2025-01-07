package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NeedCleanCoding
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@NamedEntityGraph(
        name = "Role",
        attributeNodes = {
                @NamedAttributeNode("permissions")
        }
)
public class Role extends BaseEntity {

    @Column(nullable = false)
    @Expose
    private String name;

    @ManyToMany
    @JoinTable(name = "roles_permissions",
            joinColumns = { @JoinColumn(name = "role_id") },
            inverseJoinColumns = { @JoinColumn(name = "permission_id") }
    )
    @Expose
    private Set<Permission> permissions;

//    @Transient
//    @Expose
//    private List<Long> permissionIds;

    @PostLoad
    public void initAfterLoad(){

        if(permissions != null){
            permissions.forEach(v -> v.setRoles(null));
//            permissionIds = permissions.stream().map(Permission::getId).toList();
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
        Role role = (Role) o;
        return Objects.equals(name, role.name) && Objects.equals(id, role.id);
    }
}

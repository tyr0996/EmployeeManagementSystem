package hu.martin.ems.model;

import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import hu.martin.ems.vaadin.core.NamedObject;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
@NeedCleanCoding
@NoArgsConstructor
public class Role extends BaseEntity implements NamedObject {

    @Column(nullable = false)
    @Expose
    private String name;

    @ManyToMany
    @JoinTable(name = "roles_permissions",
            joinColumns = {@JoinColumn(name = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "permission_id")}
    )
    @Expose
    private Set<Permission> permissions;

    @PostLoad
    public void initAfterLoad() {
        permissions.forEach(v -> v.setRoles(null));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name + id);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            return hashCode() == o.hashCode();
        }
        return false;
    }
}

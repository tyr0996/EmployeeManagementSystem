package hu.martin.ems.model;


import com.google.gson.annotations.Expose;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PostLoad;
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
public class Permission extends BaseEntity {
    @Column(nullable = false)
    @Expose
    private String name;

    @ManyToMany
    @Expose
    private Set<Role> roles;

    @PostLoad
    public void initAfterLoad(){
        roles.forEach(v -> v.setPermissions(null));
    }

    @Override
    public int hashCode() {
        return Objects.hash("Permission" + name + id);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Permission && hashCode() == o.hashCode();
    }
}

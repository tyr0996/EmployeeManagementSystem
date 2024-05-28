package hu.martin.ems.model;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class RoleXPermission extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "role_id", columnDefinition = "BIGINT", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "permission_id", columnDefinition = "BIGINT", nullable = false)
    private Permission permission;

    public RoleXPermission(Role r, Permission p){
        this.role = r;
        this.permission = p;
    }
}

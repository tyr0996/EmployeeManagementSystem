package hu.martin.ems.repository;

import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.model.Permission;
import hu.martin.ems.model.Role;
import hu.martin.ems.model.RoleXPermission;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface RoleXPermissionRepository extends BaseRepository<RoleXPermission, Long> {
    @Query("SELECT p FROM Permission p " +
            "LEFT JOIN RoleXPermission rxp on rxp.permission.id = p.id " +
            "WHERE rxp.role.id = :roleId")
    List<Permission> findAllPermission(@Param("roleId") Long roleId);

    @Query("SELECT r FROM Role r " +
            "LEFT JOIN RoleXPermission rxp on rxp.role.id = r.id " +
            "WHERE rxp.permission.id = :permissionId")
    List<Role> findAllRole(@Param("permissionId") Long permissionId);


    @Modifying
    @Query("DELETE FROM RoleXPermission rxp WHERE rxp.role.id = :roleId")
    void clearPermissions(@Param("roleId") Long roleId);

    @Query("SELECT new RoleXPermission(r, p) FROM Role r " +
            "LEFT JOIN RoleXPermission rxp on rxp.role.id = r.id " +
            "LEFT JOIN Permission p on p.id = rxp.permission.id")
    List<RoleXPermission> findAllWithUnused();
}

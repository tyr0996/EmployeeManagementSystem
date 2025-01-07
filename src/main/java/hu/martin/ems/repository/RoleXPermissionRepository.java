//package hu.martin.ems.repository;
//
//import hu.martin.ems.core.repository.BaseRepository;
//import hu.martin.ems.model.Permission;
//import hu.martin.ems.model.Role;
//import hu.martin.ems.model.RoleXPermission;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Repository
//@Transactional
//public interface RoleXPermissionRepository extends BaseRepository<RoleXPermission, Long> {
//    @Query("SELECT p FROM Permission p " +
//            "LEFT JOIN RoleXPermission rxp on rxp.permission.id = p.id " +
//            "WHERE rxp.role.id = :roleId")
//    List<Permission> findAllPermission(@Param("roleId") Long roleId);
//
//    @Query("SELECT r FROM Role r " +
//            "LEFT JOIN RoleXPermission rxp on rxp.role.id = r.id " +
//            "WHERE rxp.permission.id = :permissionId")
//    List<Role> findAllRole(@Param("permissionId") Long permissionId);
//
//    @Query("SELECT new RoleXPermission(r, p) FROM Role r " +
//            "LEFT JOIN RoleXPermission rxp on rxp.role.id = r.id " +
//            "LEFT JOIN Permission p on p.id = rxp.permission.id " +
//            "WHERE rxp.deleted = 0")
//    List<RoleXPermission> findAllWithUnused();
//
//    @Query("SELECT new RoleXPermission(r, p) FROM Role r " +
//            "LEFT JOIN RoleXPermission rxp on rxp.role.id = r.id " +
//            "LEFT JOIN Permission p on p.id = rxp.permission.id " +
//            "WHERE (rxp.deleted = 0 OR rxp.deleted = 1)")
//    List<RoleXPermission> findAllWithUnusedWithDeleted();
//
//    @Modifying
//    @Query("DELETE FROM RoleXPermission rxp WHERE rxp.role.id = :roleId")
//    void removeAllPermissionsFrom(@Param("roleId") Long roleId);
//
//    @Modifying
//    @Query("DELETE FROM RoleXPermission rxp WHERE rxp.permission.id = :permissionId")
//    void removeAllRolesFrom(@Param("permissionId")Long id);
//
//
//    @Query("SELECT rxp FROM RoleXPermission rxp " +
//            "WHERE rxp.role.id = :roleId")
//    List<RoleXPermission> findAlRoleXPermissionByRole(Long roleId);
//
//    @Query("SELECT rxp FROM RoleXPermission rxp " +
//            "WHERE rxp.permission.id = :permissionId")
//    List<RoleXPermission> findAllRoleXPermissionByPermission(Long permissionId);
//}

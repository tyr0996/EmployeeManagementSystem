package hu.martin.ems.repository;

import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.model.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional
public interface RoleRepository extends BaseRepository<Role, Long> {

    @Query("SELECT r FROM Role r " +
            "WHERE r.name = :name and r.deleted = 0 and r.id > 0")
    Role findByName(@Param("name") String name);

    @Query("SELECT r FROM Role r " +
            "WHERE r.name = :name and r.deleted = 0")
    Role findByNameWithNegativeId(@Param("name") String name);


}

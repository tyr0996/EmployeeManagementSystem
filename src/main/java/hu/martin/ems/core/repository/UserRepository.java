package hu.martin.ems.core.repository;

import hu.martin.ems.core.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface UserRepository extends BaseRepository<User, Long>{

    @Query("SELECT u FROM User u WHERE u.username = :userName AND u.deleted = 0")
    User findByUserName(@Param("userName") String userName);

    @Query("SELECT u FROM User u WHERE u.username = :userName AND (u.deleted = 1 OR u.deleted = 0)")
    User userExists(@Param("userName") String userName);
}

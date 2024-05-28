package hu.martin.ems.repository;

import hu.martin.ems.core.repository.BaseRepository;
import hu.martin.ems.model.CodeStore;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface CodeStoreRepository extends BaseRepository<CodeStore, Long> {

    @Query("SELECT cs FROM CodeStore cs " +
            "WHERE cs.parentCodeStore.id = :codeStoreId")
    List<CodeStore> getChildren(@Param("codeStoreId") Long codeStoreId);

    @Query("SELECT cs FROM CodeStore cs " +
            "WHERE cs.name ilike :name")
    CodeStore findByName(@Param("name") String name);
}

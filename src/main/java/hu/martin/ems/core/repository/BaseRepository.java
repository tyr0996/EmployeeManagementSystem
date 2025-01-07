package hu.martin.ems.core.repository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends Repository<T, ID> {
    List<T> customFindAll(Boolean withDeleted);

    List<T> customFindAllWithGraph(Boolean withDeleted);

    void customDelete(T entity);

    void customRestore(T entity);

    void customPermanentlyDelete(Long entity);

    @Transactional
    T customSave(T entity);

    T customUpdate(T entity) throws EntityNotFoundException;

    T customFindById(Long id);

    void customClearDatabaseTable();

    void customForcePermanentlyDelete(Long entityId);

    List<T> customFindAllById(List<Long> ids);
}
package hu.martin.ems.core.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends Repository<T, ID> {
    List<T> customSaveAll(List<T> entities);

    List<T> customFindAll(Boolean withDeleted);

    void customDelete(T entity);

    void customRestore(T entity);

    void customPermanentlyDelete(T entity);

    T customSaveOrUpdate(T entity);
}
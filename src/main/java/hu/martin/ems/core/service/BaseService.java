package hu.martin.ems.core.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.repository.BaseRepository;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NeedCleanCoding
public class BaseService<T, R extends BaseRepository<T, Long>> {
    protected R repo;

    public T save(T entity) {
        return repo.customSave(entity);
    }

    public T update(T entity) {
        return repo.customUpdate(entity);
    }

    public T delete(T entity) {
        return repo.customDelete(entity);
    }

    public T restore(T entity) {
        return repo.customRestore(entity);
    }

    public T permanentlyDelete(Long entityId) {
        return repo.customPermanentlyDelete(entityId);
    }

    public List<T> findAll(boolean withDeleted) {
        return repo.customFindAll(withDeleted);
    }

    public void clearDatabaseTable(boolean onlyPermanentlyDeleted) {
        repo.customClearDatabaseTable(onlyPermanentlyDeleted);
    }

    public T findById(Long id) {
        return repo.customFindById(id);
    }
}

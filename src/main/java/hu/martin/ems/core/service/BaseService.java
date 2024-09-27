package hu.martin.ems.core.service;

import hu.martin.ems.NeedCleanCoding;
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

    public T update(T entity){
        return repo.customUpdate(entity);
    }

    public void delete(T entity) {
        repo.customDelete(entity);
    }

    public void restore(T entity) {
        repo.customRestore(entity);
    }

    public void permanentlyDelete(Long entityId) {
        repo.customPermanentlyDelete(entityId);
    }

    public List<T> findAll(boolean withDeleted) {
        return repo.customFindAll(withDeleted);
    }

    public List<T> findAll() {
        return repo.customFindAll(true);
    }

    public T findById(Long id){
        return repo.customFindById(id);
    }

    public void clearDatabaseTable(){ repo.customClearDatabaseTable(); }
}

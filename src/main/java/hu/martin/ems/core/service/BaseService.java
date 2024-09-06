package hu.martin.ems.core.service;

import hu.martin.ems.core.repository.BaseRepository;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class BaseService<T, R extends BaseRepository<T, Long>> {

    protected R repo;

    public T save(T entity) {
        return repo.customSave(entity);
    }

    public T update(T entity){
        return repo.customUpdate(entity);
    }

    public void delete(Long entityId) {
        repo.customDelete(entityId);
    }

    public void restore(Long entityId) {
        repo.customRestore(entityId);
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
}

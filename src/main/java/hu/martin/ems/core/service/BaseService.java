package hu.martin.ems.core.service;

import hu.martin.ems.core.repository.BaseRepository;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class BaseService<T, R extends BaseRepository<T, Long>> {

    protected R repo;
    public T saveOrUpdate(T entity) {
        return repo.customSaveOrUpdate(entity);
    }
    public void delete(T entity) {
        repo.customDelete(entity);
    }

    public void restore(T entity) {
        repo.customRestore(entity);
    }

    public void permanentlyDelete(T entity) { repo.customPermanentlyDelete(entity); }
    public List<T> findAll(boolean withDeleted) { return repo.customFindAll(withDeleted); }
    public List<T> findAll() { return repo.customFindAll(true); }
}

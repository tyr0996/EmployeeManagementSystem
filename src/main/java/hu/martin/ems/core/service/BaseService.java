package hu.martin.ems.core.service;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.repository.BaseRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NeedCleanCoding
public class BaseService<T, R extends BaseRepository<T, Long>> {

    @Setter
    @Getter
    protected R repo;

    public T save(T entity) {
        return repo.customSave(entity);
    }

    public T update(T entity){
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

//    public T findById(Long id){
//        return repo.customFindById(id);
//    }

    public List<T> findAllByIds(List<Long> ids){
        return repo.customFindAllById(ids);
    }

    public void clearDatabaseTable(){ repo.customClearDatabaseTable(); }

    public void forcePermanentlyDelete(Long entityId) {
        repo.customForcePermanentlyDelete(entityId);
    }

    public T findById(Long id) {
        return repo.customFindById(id);
    }
}

package hu.martin.ems.core.repository;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@NoRepositoryBean
@Transactional
@NeedCleanCoding
public class BaseRepositoryImpl<T extends BaseEntity, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    protected final EntityManager entityManager;
    private final Logger logger;
    private Class<T> type;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.logger = LoggerFactory.getLogger(entityInformation.getJavaType());
        this.type = entityInformation.getJavaType();
        this.entityManager = entityManager;
    }

    @Override
    public List<T> customFindAll(Boolean withDeleted) {
        boolean includeDeleted = withDeleted != null ? withDeleted : true;
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e";
        if (!includeDeleted) {
            jpql += " WHERE e.deleted = 0";
        }
        return entityManager.createQuery(jpql, type).getResultList();
    }

    @Override
    public void customDelete(Long entityId) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 1 WHERE e.id = :id")
                .setParameter("id", entityId)
                .executeUpdate();
        logger.info(type.getSimpleName() + " deleted successfully: {}", entityId);
    }

    @Override
    public void customRestore(Long entityId) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 0 WHERE e.id = :id")
                .setParameter("id", entityId)
                .executeUpdate();
        logger.info(type.getSimpleName() + " restored successfully: {}", entityId);
    }

    @Override
    public void customPermanentlyDelete(Long entityId) {
        entityManager.createQuery("DELETE FROM " + type.getSimpleName() + " e WHERE e.id = :id")
                .setParameter("id", entityId)
                .executeUpdate();
        logger.info(type.getSimpleName() + " deleted permanently successfully: {}", entityId);
    }

    @Override
    public T customSave(T entity) {
        T savedEntity = super.save(entity);
        logger.info(savedEntity.getClass().getSimpleName() + " saved successfully: {}", savedEntity);
        return savedEntity;
    }

    @Override
    public T customUpdate(T entity) throws EntityNotFoundException{
        if (customFindById(entity.getId()) != null) {
            throw new EntityNotFoundException("Entity with type " + type.getSimpleName() + " not found: " + entity);
        }
        T updatedEntity = super.save(entity);
        logger.info(updatedEntity.getClass().getSimpleName() + " updated successfully: {}", updatedEntity);
        return updatedEntity;
    }

    @Override
    public T customFindById(Long entityId){
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e.id = " + entityId;
        return entityManager.createQuery(jpql, type).getSingleResult();
    }
}
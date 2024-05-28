package hu.martin.ems.core.repository;

import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.EntityManager;
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
    public List<T> customSaveAll(List<T> entities) {
        List<T> savedEntities = super.saveAll(entities);
        logger.info("Entities saved successfully: {}", savedEntities);
        return savedEntities;
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
    public void customDelete(T entity) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 1 WHERE e.id = :id")
                     .setParameter("id", entity.getId())
                     .executeUpdate();
        logger.info(type.getSimpleName() + " deleted successfully: {}", entity);
    }

    @Override
    public void customRestore(T entity) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 0 WHERE e.id = :id")
                     .setParameter("id", entity.getId())
                     .executeUpdate();
        logger.info(type.getSimpleName() + " restored successfully: {}", entity);
    }

    @Override
    public void customPermanentlyDelete(T entity) {
        entityManager.createQuery("DELETE FROM " + type.getSimpleName() + " e WHERE e.id = :id")
                                   .setParameter("id", entity.getId())
                                   .executeUpdate();
        logger.info(type.getSimpleName() + " deleted permanently successfully: {}", entity);
    }

    @Override
    public T customSaveOrUpdate(T entity) {
        T savedOrUpdatedEntity = super.save(entity);
        logger.info(savedOrUpdatedEntity.getClass().getSimpleName() + " saved or updated successfully: {}", savedOrUpdatedEntity);
        return savedOrUpdatedEntity;
    }
}
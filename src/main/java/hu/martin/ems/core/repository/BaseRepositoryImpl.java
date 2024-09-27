package hu.martin.ems.core.repository;

import hu.martin.ems.NeedCleanCoding;
import hu.martin.ems.core.config.BeanProvider;
import hu.martin.ems.core.config.JPAConfig;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.JDBCException;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.provider.HibernateUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

@NoRepositoryBean
@NeedCleanCoding
@Lazy
public class BaseRepositoryImpl<T extends BaseEntity, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    @PersistenceContext
    protected final EntityManager entityManager;
    private final Logger logger;
    private Class<T> type;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.logger = LoggerFactory.getLogger(entityInformation.getJavaType());
        this.type = entityInformation.getJavaType();
        this.entityManager = entityManager;
    }


    @Transactional
    @Override
    public List<T> customFindAll(Boolean withDeleted) {
        boolean includeDeleted = withDeleted != null ? withDeleted : true;
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e";
        if (!includeDeleted) {
            jpql += " WHERE e.deleted = 0";
        }
        else{
            jpql += " WHERE e.deleted = 1 OR e.deleted = 0";
        }
        return entityManager.createQuery(jpql, type).getResultList();
    }


    @Transactional
    @Override
    public void customDelete(T entity) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 1 WHERE e.id = :id")
                .setParameter("id", entity.getId())
                .executeUpdate();
        logger.info(type.getSimpleName() + " deleted successfully: {}", entity.getId());
    }


    @Transactional
    @Override
    public void customRestore(T entity) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 0 WHERE e.id = :id")
                .setParameter("id", entity.getId())
                .executeUpdate();
        logger.info(type.getSimpleName() + " restored successfully: {}", entity.getId());
    }


    @Transactional
    @Override
    public void customPermanentlyDelete(Long entityId) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 2 WHERE e.id = :id")
                .setParameter("id", entityId)
                .executeUpdate();
        logger.info(type.getSimpleName() + " deleted permanently successfully: {}", entityId);
    }


    @Transactional
    @Override
    public T customSave(T entity) {
        T savedEntity = super.save(entity);
        logger.info(savedEntity.getClass().getSimpleName() + " saved successfully: {}", savedEntity);
        return savedEntity;
    }


    @Transactional
    @Override
    public T customUpdate(T entity) throws EntityNotFoundException{
        if (customFindById(entity.getId()) == null) {
            throw new EntityNotFoundException("Entity with type " + type.getSimpleName() + " not found: " + entity);
        }
        T updatedEntity = super.save(entity);
        logger.info(updatedEntity.getClass().getSimpleName() + " updated successfully: {}", updatedEntity);
        return updatedEntity;
    }


    @Transactional
    @Override
    public T customFindById(Long entityId){
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e.id = " + entityId;
        return entityManager.createQuery(jpql, type).getSingleResult();
    }

    @Transactional
    @Override
    public void customClearDatabaseTable() {
        String permanentlyDeletedElementsJpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e.deleted = 2";
        List<T> permanentlyDeletedElements = entityManager.createQuery(permanentlyDeletedElementsJpql, type).getResultList();
        int clearedElements = 0;

        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();

        for (T element : permanentlyDeletedElements) {
            clearedElements += deleteEntity(tempEm, element);
        }
        logger.info(clearedElements + " element(s) successfully removed from database table.");
    }

    public int deleteEntity(EntityManager tempEm, T entity) {
        int affected = 0;
        EntityTransaction transaction = tempEm.getTransaction();
        try{
            transaction.begin();
            T en = tempEm.find(type, entity.getId());
            tempEm.remove(en);
            transaction.commit();
            affected = 1;
        }
        catch (Exception e){
            if (e.getCause().getMessage().contains("violates foreign key constraint")) {
                logger.info("Entity with ID " + entity.getId() + " is not deletable due to it has reference(s) in other table(s)");
            }
            else{
                logger.error("Entity with ID " + entity.getId() + " is not deletable due to a fatal error. It needs to be debugged.");
                e.getCause().printStackTrace();
                if(transaction.isActive()){
                    transaction.rollback();
                }
            }
        }
        finally{
            if(tempEm.isOpen()){
                tempEm.clear();
            }
        }
        return affected;
    }

}
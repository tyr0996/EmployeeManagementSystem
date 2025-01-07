package hu.martin.ems.core.repository;

import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Setter;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

@NoRepositoryBean
@NeedCleanCoding
@Lazy
public class BaseRepositoryImpl<T extends BaseEntity, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    @PersistenceContext
    @Setter
    protected EntityManager entityManager;

    @Autowired
    private SessionFactory sessionFactory;

    private final Logger logger;
    private Class<T> type;

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
        List<T> result = entityManager.createQuery(jpql, type).getResultList();
        return result;
    }

    @Override
    public List<T> customFindAllWithGraph(Boolean withDeleted){
        EntityGraph<?> entityGraph = entityManager.getEntityGraph(type.getSimpleName());

        boolean includeDeleted = Objects.requireNonNullElse(withDeleted, true);
//        boolean includeDeleted = withDeleted != null ? withDeleted : true;
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e";
        if (!includeDeleted) {
            jpql += " WHERE e.deleted = 0";
        }
        else{
            jpql += " WHERE e.deleted = 1 OR e.deleted = 0";
        }
        List<T> result = entityManager.createQuery(jpql, type)
                .setHint("jakarta.persistence.fetchgraph", entityGraph)
                .getResultList();
        return result;
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
        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();

        EntityTransaction transaction = tempEm.getTransaction();
        transaction.begin();
        T merged = tempEm.merge(entity);
        tempEm.persist(merged);
        transaction.commit();
        if(tempEm.isOpen()){
            tempEm.clear();
            tempEm.close();
        }
        return entity;
    }


    @Transactional
    @Override
    public T customUpdate(T entity) throws EntityNotFoundException {
        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();

        EntityTransaction transaction = tempEm.getTransaction();
        T managedEntity = tempEm.find(type, entity.getId());
        transaction.begin();
        tempEm.merge(managedEntity);

        T copied = copyEntity(managedEntity, entity, type);
        //entityManager.flush();
//        managedEntity = copyEntity(managedEntity, entity, type);
//        tempEm.remove(managedEntity);
//        tempEm.merge(managedEntity);
//        tempEm.clear();
//        tempEm.merge(entity);
        tempEm.merge(copied);
        transaction.commit();
        if(tempEm.isOpen()){
//            tempEm.clear();
            tempEm.close();
        }
        logger.info(entity.getClass().getSimpleName() + " updated successfully: {}", entity);
        return entity;
//        if (customFindById(entity.getId()) == null) {
//            throw new EntityNotFoundException("Entity with type " + type.getSimpleName() + " not found: " + entity);
//        }
//        T updatedEntity = super.save(entity);
//          return updatedEntity;
    }




    private <T> T copyEntity(T managedEntity, T newEntity, Class<T> entityType) {
        try {
            for (Field field : entityType.getDeclaredFields()) {
                field.setAccessible(true);
                field.set(managedEntity, field.get(newEntity));
            }
            return managedEntity;
        } catch (Exception e) {
            throw new RuntimeException("Entity copy failed", e);
        }
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

    @Override
    public void customForcePermanentlyDelete(Long entityId) {
        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();

        EntityTransaction transaction = tempEm.getTransaction();
        try{
            transaction.begin();
            T en = tempEm.find(type, entityId);
            tempEm.remove(en);
            transaction.commit();
            logger.info("Entity with ID " + entityId + " forced to permanently delete");
        }
        catch (Exception e){
            if (e.getCause().getMessage().contains("violates foreign key constraint")) {
                logger.info("Entity with ID " + entityId + " is not deletable due to it has reference(s) in other table(s)");
            }
            else{
                logger.error("Entity with ID " + entityId + " is not deletable due to a fatal error. It needs to be debugged.");
                e.getCause().printStackTrace();
                if(transaction.isActive()){
                    transaction.rollback();
                }
            }

        }
        finally{
            if(tempEm.isOpen()){
                tempEm.clear();
                tempEm.close();
            }
        }
    }

    @Override
    @Transactional
    public List<T> customFindAllById(List<Long> ids) {
        String idsAsString = String.join(", ", ids.stream().map(v -> v.toString()).toList());
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e.id in (" + idsAsString + ")";
        return entityManager.createQuery(jpql, type).getResultList();
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
package hu.martin.ems.core.repository;

import com.google.gson.Gson;
import hu.martin.ems.annotations.NeedCleanCoding;
import hu.martin.ems.core.config.JsonConfig;
import hu.martin.ems.core.model.BaseEntity;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

@NoRepositoryBean
@NeedCleanCoding
@Lazy
public class BaseRepositoryImpl<T extends BaseEntity, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {

    @PersistenceContext
    protected EntityManager entityManager;

    private final Logger logger;
    private Class<T> type;

    protected Gson gson;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.logger = LoggerFactory.getLogger(entityInformation.getJavaType());
        this.type = entityInformation.getJavaType();
        this.entityManager = entityManager;
        JsonConfig jsonConfig = new JsonConfig();
        this.gson = jsonConfig.gson();
    }

    @Transactional
    @Override
    public List<T> customFindAll(boolean withDeleted) {
        boolean includeDeleted = withDeleted;
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



    @Transactional
    @Override
    public T customDelete(T entity) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 1 WHERE e.id = :id")
                .setParameter("id", entity.getId())
                .executeUpdate();
        T deleted = entityManager.createQuery("SELECT e FROM " + type.getSimpleName() + " e WHERE e.id = :id", type)
                .setParameter("id", entity.getId())
                .getSingleResult();
        logger.info(type.getSimpleName() + " deleted successfully: {}", entity.getId());
        return deleted;
    }


    @Transactional
    @Override
    public T customRestore(T entity) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 0 WHERE e.id = :id")
                .setParameter("id", entity.getId())
                .executeUpdate();
        T restored = entityManager.createQuery("SELECT e FROM " + type.getSimpleName() + " e WHERE e.id = :id", type)
                .setParameter("id", entity.getId())
                .getSingleResult();
        logger.info(type.getSimpleName() + " restored successfully: {}", entity.getId());
        return restored;
    }


    @Transactional
    @Override
    public T customPermanentlyDelete(Long entityId) {
        entityManager.createQuery("UPDATE " + type.getSimpleName() + " e SET e.deleted = 2 WHERE e.id = :id")
                .setParameter("id", entityId)
                .executeUpdate();
        T permanentlyDeleted = entityManager.createQuery("SELECT e FROM " + type.getSimpleName() + " e WHERE e.id = :id", type)
                .setParameter("id", entityId)
                .getSingleResult();
        logger.info(type.getSimpleName() + " deleted permanently successfully: {}", entityId);
        return permanentlyDeleted;
    }


    @Transactional
    @Override
    public T customSave(T entity) {
        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();
        EntityTransaction transaction = tempEm.getTransaction();
        try{
            transaction.begin();
            T merged = tempEm.merge(entity);
            tempEm.persist(merged);
            transaction.commit();
            logger.info("Entity " + entity.getClass().getSimpleName() + " saved successfully: " + gson.toJson(entity));
            return merged;
        } catch (Exception e) {
//            if (transaction.isActive()) {
//                transaction.rollback();
//            }
            throw e;
        } finally {
//            if (tempEm.isOpen()) {
                tempEm.close();
//            }
        }

    }


    @Transactional
    @Override
    public T customUpdate(T entity) {
        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();
        EntityTransaction transaction = tempEm.getTransaction();
        try{
            T managedEntity = tempEm.find(type, entity.getId());
            transaction.begin();
//            tempEm.merge(managedEntity);

            copyEntity(managedEntity, entity, type);

            T result = tempEm.merge(managedEntity);
            transaction.commit();
            logger.info("{} updated successfully: {}", entity.getClass().getSimpleName(), entity);
            return result;
        } catch (Exception e) {
            logger.error("{} with id {} update failed: {}", entity.getClass().getSimpleName(), entity.getId(), e.getMessage());
            throw new RuntimeException(e);
        } finally {
            tempEm.close();
        }
    }




    private <T> T copyEntity(T managedEntity, T newEntity, Class<T> entityType) throws IllegalAccessException {
        for (Field field : entityType.getDeclaredFields()) {
            field.setAccessible(true);
            field.set(managedEntity, field.get(newEntity));
        }
        return managedEntity;
    }


    @Transactional
    @Override
    public T customFindById(Long entityId){
        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();
        String jpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e.id = " + entityId;
        T result = tempEm.createQuery(jpql, type).getSingleResult();;
        tempEm.close();
        return result;
    }

    @Transactional
    @Override
    public void customClearDatabaseTable(boolean onlyPermanentlyDeleted) {
        String permanentlyDeletedElementsJpql = "SELECT e FROM " + type.getSimpleName() + " e WHERE e.deleted = 2";
        String allElementsJpql = "SELECT e FROM " + type.getSimpleName() + " e";
        List<T> elementsToBeDeleted = entityManager.createQuery(onlyPermanentlyDeleted ? permanentlyDeletedElementsJpql : allElementsJpql, type).getResultList();
        int clearedElements = 0;

        EntityManagerFactory factory = entityManager.getEntityManagerFactory();
        EntityManager tempEm = factory.createEntityManager();

        for (T element : elementsToBeDeleted) {
            clearedElements += deleteEntity(tempEm, element);
        }
        tempEm.close();
        logger.info(clearedElements + " element(s) successfully removed from database table.");
    }


    private int deleteEntity(EntityManager tempEm, T entity) {
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
            logger.info("Entity with ID " + entity.getId() + " is not deletable due to it has reference(s) in other table(s)");
        }
//        finally{
////            if(tempEm.isOpen()){
////                tempEm.clear();
//                tempEm.close();
////            }
//        }
        return affected;
    }
}
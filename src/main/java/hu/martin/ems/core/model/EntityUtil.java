package hu.martin.ems.core.model;

import java.lang.reflect.Field;

@Deprecated(forRemoval = true)
public class EntityUtil<T> {
//    public T copyEntity(T managedEntity, T newEntity, Class<T> entityType) {
//        for (Field field : entityType.getFields()) {
//            if (field.getName().startsWith("__$")) {
//                continue;
//            }
//            Boolean originalAccessible = field.canAccess(managedEntity);
//            try {
//                field.setAccessible(true);
//                this.copyField(managedEntity, newEntity, field);
//            } catch (IllegalAccessException e) {
//                throw new RuntimeException("Failed to copy field: " + field.getName(), e);
//            } finally {
//                field.setAccessible(originalAccessible);
//            }
//        }
//        return managedEntity;
//    }
//
//    public void copyField(T managedEntity, T newEntity, Field f) throws IllegalAccessException {
//        f.set(managedEntity, f.get(newEntity));
//    }
}

package io.github.mgluizbrito.topsoil_seeder.mapper;

import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Utility class responsible for translating a map of properties into a fully
 * instantiated entity object using Java Reflection.
 */
public class EntityMapper {

    /**
     * Maps a given map of Key-Value pairs into an instance of a specified Class.
     * The keys in the map must match the field names in the target class.
     *
     * @param clazz The target class to be instantiated. Must provide a no-args
     *              constructor.
     * @param data  A map containing the fields and properties to inject into the
     *              new entity.
     * @param <T>   The type of the target entity.
     * @return A new instance of {@code T} populated with the provided data.
     * @throws ReflectiveOperationException if the class cannot be instantiated or
     *                                      fields are inaccessible.
     */
    public <T> T mapToEntity(@NonNull Class<T> clazz, @NonNull Map<String, Object> data) throws ReflectiveOperationException {
        T entity = clazz.getDeclaredConstructor().newInstance();

        data.forEach((key, value) -> setField(entity, key, value));
        return entity;
    }

    /**
     * Sets a specific field value on the target entity via reflection, navigating
     * class hierarchies
     * if the field originates in a superclass.
     *
     * @param entity    The target object instance.
     * @param fieldName The name of the field to inject the value into.
     * @param value     The value to inject.
     */
    private void setField(@NonNull Object entity, String fieldName, Object value) {
        try {
            Field field = this.findField(entity.getClass(), fieldName);
            field.setAccessible(true);
            field.set(entity, value);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error defining field '" + fieldName + "' in entity " + entity.getClass().getSimpleName(), e);
        }
    }

    /**
     * Recursively searches for a declared field in the target class and its
     * superclasses.
     *
     * @param clazz     The class to inspect.
     * @param fieldName The name of the field to locate.
     * @return The declared {@link Field}.
     * @throws NoSuchFieldException if the field is not found in the class or its
     *                              hierarchy.
     */
    private Field findField(@NonNull Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);

        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null)
                return findField(clazz.getSuperclass(), fieldName);
            throw e;
        }
    }
}

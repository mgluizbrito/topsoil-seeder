package io.github.mgluizbrito.topsoil_seeder.mapper;

import lombok.NonNull;

import java.lang.reflect.Field;
import java.util.Map;

public class EntityMapper {

    public <T> T mapToEntity(@NonNull Class<T> clazz, @NonNull Map<String, Object> data) throws ReflectiveOperationException {
        T entity = clazz.getDeclaredConstructor().newInstance();

        data.entrySet().forEach(entry -> setField(entry, entry.getKey(), entry.getValue()));
        return entity;
    }

    private void setField(@NonNull Object entity, String fieldName, Object value) {
        try {
            Field field = this.findField(entity.getClass(), fieldName);
            field.setAccessible(true);
            field.set(entity, value);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("error defining field " + fieldName + " in entity " + entity.getClass().getSimpleName(), e);
        }
    }

    private Field findField(@NonNull Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);

        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) return findField(clazz.getSuperclass(), fieldName);
            throw e;
        }
    }
}

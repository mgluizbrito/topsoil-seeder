package io.github.mgluizbrito.topsoil_seeder.core.exception;

public class EntityClassNotFoundException extends RuntimeException {
    public EntityClassNotFoundException(String entityName, String basePackage) {
        super(String.format("Entity '%s' not found. " +
                "Verify that the name is correct or that the basePackage '%s' is configured correctly.",
                entityName, basePackage));
    }

    public EntityClassNotFoundException(String entityName) {
        super(String.format("Entity '%s' not found. Since no basePackage has been defined, " +
                "you must use the full class name (e.g. com.app.model.User).", entityName));
    }
}

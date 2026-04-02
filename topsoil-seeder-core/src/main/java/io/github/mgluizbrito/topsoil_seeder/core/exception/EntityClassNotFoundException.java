package io.github.mgluizbrito.topsoil_seeder.core.exception;

public class EntityClassNotFoundException extends RuntimeException {
    public EntityClassNotFoundException(String entityName) {
        super(String.format("Entity '%s' not found dynamically via internal JPA Metamodel registry. " +
                "Verify that the name is correct, that the class is annotated with @Entity, " +
                "or try providing the full class path (e.g. com.app.model.User) in the YAML file.", entityName));
    }
}

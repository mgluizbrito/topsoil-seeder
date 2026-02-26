package io.github.mgluizbrito.topsoil_seeder_core.exception;

/**
 * Exception thrown when a temporary YAML ID referenced using '@'
 * cannot be found in the active ReferenceResolver session.
 */
public class ReferenceNotFound extends RuntimeException {
    public ReferenceNotFound(String yamlId) {
        super("reference not found: " + yamlId);
    }
}

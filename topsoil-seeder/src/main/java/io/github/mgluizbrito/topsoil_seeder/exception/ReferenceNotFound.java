package io.github.mgluizbrito.topsoil_seeder.exception;

public class ReferenceNotFound extends RuntimeException {
    public ReferenceNotFound(String yamlId) {
        super("reference not found" + yamlId);
    }
}

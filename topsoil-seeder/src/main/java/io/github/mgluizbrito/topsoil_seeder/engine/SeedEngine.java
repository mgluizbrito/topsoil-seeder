package io.github.mgluizbrito.topsoil_seeder.engine;

import io.github.mgluizbrito.topsoil_seeder.mapper.EntityMapper;
import io.github.mgluizbrito.topsoil_seeder.yaml.ReferenceResolver;
import io.github.mgluizbrito.topsoil_seeder.yaml.YamlParser;
import io.github.mgluizbrito.topsoil_seeder.yaml.YamlResourcesScanner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main library engine. Orchestrates the reading of YAML files,
 * reference resolution and persistence in the database.
 */
public class SeedEngine {

    private static final Logger logger = Logger.getLogger(SeedEngine.class.getName());
    private final EntityManager manager;
    private final EntityMapper mapper;
    private final ReferenceResolver resolver;

    public SeedEngine(EntityManager manager) {
        this.manager = manager;
        this.mapper = new EntityMapper();
        this.resolver = new ReferenceResolver();
    }

    /**
     * Runs the seeding process from "src/main/resources/seeds" by default.
     */
    public void seed() {
        seed("seeds");
    }

    /**
     * Runs the seeding process from a folder in resources.
     * @param resourceFolder Folder path "src/main/resources/{folderName}"
     */
    public void seed(String resourceFolder) {
        EntityTransaction transaction = manager.getTransaction();

        try {
            transaction.begin();
            logger.info("Starting seeding process from: src/main/resources/" + resourceFolder);

            // 1. Scan: Finds file names and opens InputStreams for found files
            List<InputStream> yamlStreams = YamlResourcesScanner.loadYamlResources(resourceFolder);

            // 2. Parse: Transforms raw YAML into Data Maps
            List<Map<String, Object>> yamlData = YamlParser.parseYamlFiles(yamlStreams);

            // 4. Process: Iterates over each entity block defined in YAML
            for (Map<String, Object> block : yamlData) processEntityBlock(block);

            transaction.commit();
            logger.info("Seeding completed successfully");

        } catch (IOException | URISyntaxException | ReflectiveOperationException e) {

            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Critical error during seeding. Operation aborted.", e);
            throw new RuntimeException("Database populate failure", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void processEntityBlock(@NonNull Map<String, Object> block) throws ReflectiveOperationException {
        String entityClassName = (String) block.get("entity");
        List<Map<String, Object>> records = (List<Map<String, Object>>) block.get("data");

        if (entityClassName == null || records == null) {
            logger.warning("Invalid YAML block detected. Next...");
            return;
        }

        Class<?> clazz = Class.forName(entityClassName);
        logger.info("Processing " + records.size() + " records for: " + entityClassName);

        for (Map<String, Object> data : records) {
            // Captures the YAML temporary ID (e.g. "user-1") if it exists
            String yamlId = (String) data.remove("_id");

            // Resolves "@references" before mapping to entity
            resolver.resolveReferences(data);

            // Transform the Map into an Entity Object via Reflection
            Object entity = mapper.mapToEntity(clazz, data);

            // Database persistence
            manager.persist(entity);
            manager.flush();

            if (yamlId != null) {
                Object realId = manager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
                resolver.registerReference(yamlId, entity);
                logger.fine("Registered reference: " + yamlId + " -> " + realId);
            }
        }
    }
}

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
 * Main engine of the Topsoil Seeder library.
 * This class orchestrates reading YAML files, resolving inter-entity
 * references,
 * and persisting the seeded data into the database using JPA.
 */
public class SeedEngine {

    private static final Logger logger = Logger.getLogger(SeedEngine.class.getName());
    private final EntityManager manager;
    private final EntityMapper mapper;
    private final ReferenceResolver resolver;

    /**
     * Constructs a SeedEngine using the provided EntityManager.
     *
     * @param manager the JPA EntityManager used to persist seeded entities.
     */
    public SeedEngine(EntityManager manager) {
        this.manager = manager;
        this.mapper = new EntityMapper();
        this.resolver = new ReferenceResolver();
    }

    /**
     * Executes the seeding process using the default "seeds" folder in the
     * classpath.
     * Scans for all `.yaml` and `.yml` files within "src/main/resources/seeds".
     *
     * @throws RuntimeException if a critical error occurs during file reading,
     *                          parsing, or database persistence.
     */
    public void seed() {
        seed("seeds");
    }

    /**
     * Executes the seeding process from a specific resource folder.
     *
     * @param resourceFolder The folder path relative to "src/main/resources" (e.g.,
     *                       "my-seeds").
     * @throws RuntimeException if a critical error occurs during file reading,
     *                          parsing, or database persistence.
     */
    public void seed(String resourceFolder) {
        EntityTransaction transaction = manager.getTransaction();

        try {
            transaction.begin();
            logger.info("Starting seeding process from configuration folder: " + resourceFolder);

            // 1. Scan: Finds file names and opens InputStreams for found files
            List<InputStream> yamlStreams = YamlResourcesScanner.loadYamlResources(resourceFolder);

            // 2. Parse: Transforms raw YAML into list of Maps
            List<Map<String, Object>> yamlData = YamlParser.parseYamlFiles(yamlStreams);

            // 3. Process: Iterates over each entity block defined in the YAML
            // configurations
            for (Map<String, Object> block : yamlData)
                processEntityBlock(block);

            transaction.commit();
            logger.info("Seeding completed successfully.");

        } catch (IOException | URISyntaxException | ReflectiveOperationException e) {

            if (transaction.isActive()) transaction.rollback();
            logger.log(Level.SEVERE, "Critical error during seeding. Operation aborted.", e);
            throw new RuntimeException("Database population failed.", e);
        }
    }

    /**
     * Processes a single YAML block configuring an entity and its associated data
     * records.
     *
     * @param block A Map containing the "entity" class name and a list of "data"
     *              maps to persist.
     * @throws ReflectiveOperationException if class instantiation or field mapping
     *                                      fails.
     */
    @SuppressWarnings("unchecked")
    private void processEntityBlock(@NonNull Map<String, Object> block) throws ReflectiveOperationException {
        String entityClassName = (String) block.get("entity");
        List<Map<String, Object>> records = (List<Map<String, Object>>) block.get("data");

        if (entityClassName == null || records == null) {
            logger.warning("Invalid YAML block detected (missing 'entity' or 'data'). Skipping...");
            return;
        }

        Class<?> clazz = Class.forName(entityClassName);
        logger.info("Processing " + records.size() + " records for entity: " + entityClassName);

        for (Map<String, Object> data : records) {
            // Captures the YAML temporary ID (e.g. "_id: user-1") if it exists for
            // cross-referencing
            String yamlId = (String) data.remove("_id");

            // Resolves any "@references" to already persisted entities
            resolver.resolveReferences(data);

            // Transforms the Map into an actual Entity Object via Reflection
            Object entity = mapper.mapToEntity(clazz, data);

            // Persists the entity using the EntityManager
            manager.persist(entity);
            manager.flush();

            // If a YAML temporary ID was provided, register the newly generated database ID
            // for future references
            if (yamlId != null) {
                Object realId = manager.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
                resolver.registerReference(yamlId, entity);
                logger.fine("Registered reference mapping: " + yamlId + " -> " + realId);
            }
        }
    }
}

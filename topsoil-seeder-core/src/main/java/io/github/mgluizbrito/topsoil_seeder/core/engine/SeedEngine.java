package io.github.mgluizbrito.topsoil_seeder.core.engine;

import io.github.mgluizbrito.topsoil_seeder.core.exception.EntityClassNotFoundException;
import io.github.mgluizbrito.topsoil_seeder.core.exception.ReferenceNotFound;
import io.github.mgluizbrito.topsoil_seeder.core.mapper.EntityMapper;
import io.github.mgluizbrito.topsoil_seeder.core.yaml.ReferenceResolver;
import io.github.mgluizbrito.topsoil_seeder.core.yaml.YamlParser;
import io.github.mgluizbrito.topsoil_seeder.core.yaml.YamlResourcesScanner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.NonNull;
import lombok.Setter;

import java.io.InputStream;
import java.util.HashMap;
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

    @Setter
    private boolean manageTransactions = true;
    private Map<String, Class<?>> entityClassRegistry;

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
     * @throws EntityClassNotFoundException if an entity in YAML does not exist in any class within the set basePackage
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
     * @throws EntityClassNotFoundException if an entity in YAML does not exist in any class within the set basePackage
     * @throws RuntimeException if a critical error occurs during file reading,
     *                          parsing, or database persistence.
     */
    public void seed(String resourceFolder) {
        EntityTransaction transaction = null;

        if (manageTransactions) {
            transaction = manager.getTransaction();
            transaction.begin();
        }

        try {
            logger.info("Starting seeding process from configuration folder: " + resourceFolder);

            // Initializes the entity registry from the JPA metamodel if not already done
            initializeRegistry();

            // 1. Scan: Finds file names and opens InputStreams for found files
            List<InputStream> yamlStreams = YamlResourcesScanner.loadYamlResources(resourceFolder);

            // 2. Parse: Transforms raw YAML into list of Maps
            List<Map<String, Object>> yamlData = YamlParser.parseYamlFiles(yamlStreams);

            // 3. Process: Iterates over each entity block defined in the YAML
            // configurations
            for (Map<String, Object> block : yamlData) processEntityBlock(block);

            if (manageTransactions && transaction != null) {
                transaction.commit();
            }
            logger.info("Seeding completed successfully.");

        } catch (EntityClassNotFoundException | ReferenceNotFound e) {
            logger.log(Level.SEVERE, "Critical error during seeding. Operation aborted.", e);
            if (manageTransactions && transaction != null && transaction.isActive()) transaction.rollback();
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Critical error during seeding. Operation aborted.", e);
            if (manageTransactions && transaction != null && transaction.isActive()) transaction.rollback();
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
    private void processEntityBlock(@NonNull Map<String, Object> block) throws ReflectiveOperationException, ReferenceNotFound {
        String entityClassName = (String) block.get("entity");
        List<Map<String, Object>> records = (List<Map<String, Object>>) block.get("data");

        if (entityClassName == null || records == null) {
            logger.warning("Invalid YAML block detected (missing 'entity' or 'data'). Skipping...");
            return;
        }

        Class<?> clazz = resolveClassPackage(entityClassName);
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

    /**
     * Pre-loads a mapping of simple and fully qualified class names to their
     * actual Entity Types using the JPA Metamodel.
     */
    private void initializeRegistry() {
        if (entityClassRegistry == null) {
            entityClassRegistry = new HashMap<>();
            for (jakarta.persistence.metamodel.EntityType<?> entityType : manager.getMetamodel().getEntities()) {
                Class<?> javaType = entityType.getJavaType();
                if (javaType != null) {
                    entityClassRegistry.put(javaType.getSimpleName(), javaType);
                    entityClassRegistry.put(javaType.getName(), javaType);
                }
            }
        }
    }

    /**
     * Try to load the class by checking the loaded Metamodel registry.
     * Supports looking up by Simple Name or Fully Qualified Name.
     */
    private @NonNull Class<?> resolveClassPackage(String entityName) {
        // Fallback to Class.forName if not in registry
        // The registry requires entities to be properly mapped via @Entity.
        Class<?> clazz = null;
        if (entityClassRegistry != null) {
            clazz = entityClassRegistry.get(entityName);
        }
        
        if (clazz == null) {
            try {
                return Class.forName(entityName);
            } catch (ClassNotFoundException e) {
                throw new EntityClassNotFoundException(entityName);
            }
        }
        
        return clazz;
    }
}

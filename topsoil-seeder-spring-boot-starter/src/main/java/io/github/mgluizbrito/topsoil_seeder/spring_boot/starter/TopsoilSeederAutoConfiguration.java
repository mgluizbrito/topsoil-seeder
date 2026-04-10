package io.github.mgluizbrito.topsoil_seeder.spring_boot.starter;

import io.github.mgluizbrito.topsoil_seeder.core.engine.SeedEngine;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfiguration
@EnableConfigurationProperties(TopsoilSeederProperties.class)
@ConditionalOnProperty(prefix = "topsoil.seeder", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TopsoilSeederAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SeedEngine seedEngine(EntityManager entityManager) {
        SeedEngine engine = new SeedEngine(entityManager);
        return engine;
    }

    @Bean
    public CommandLineRunner autoSeedRunner(SeedEngine engine, TopsoilSeederProperties props, TransactionTemplate transactionTemplate) {
        return args -> {
            if (props.isEnabled()) {
                System.out.println("Topsoil Seeder: Starting automatic process...");
                transactionTemplate.execute(status -> {
                    engine.setManageTransactions(false);
                    engine.seed(props.getSeedFolder());
                    return null;
                });
            }
        };
    }
}

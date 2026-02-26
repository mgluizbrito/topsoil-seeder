package io.github.mgluizbrito.topsoil_seeder.spring_boot.starter;

import io.github.mgluizbrito.topsoil_seeder.core.engine.SeedEngine;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(TopsoilSeederProperties.class)
@ConditionalOnProperty(prefix = "topsoil.seeder", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TopsoilSeederAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SeedEngine seedEngine(EntityManager entityManager, TopsoilSeederProperties props) {

        SeedEngine engine = new SeedEngine(entityManager);
        engine.setBasePackage(props.getBasePackage());

        return engine;
    }

    @Bean
    public CommandLineRunner autoSeedRunner(SeedEngine engine, TopsoilSeederProperties props) {
        return args -> {
            engine.seed(props.getSeedFolder());
        };
    }
}

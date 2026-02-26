package io.github.mgluizbrito.topsoil_seeder.spring_boot.starter;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "topsoil.seeder")
@Getter
@Setter
public class TopsoilSeederProperties {

    /**
     * Base package for JPA entities (e.g. 'com.example.model').
     */
    private String basePackage;


    /**
     * Folder in resources containing YAML files.
     */
    private String seedFolder = "seeds";

    /**
     * Whether to enable the database seeder.
     */
    private boolean enabled = true;
}

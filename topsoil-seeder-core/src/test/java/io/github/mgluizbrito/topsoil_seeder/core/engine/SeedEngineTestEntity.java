package io.github.mgluizbrito.topsoil_seeder.core.engine;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class SeedEngineTestEntity {

    @Id
    private Long id;
    private String name;
    private Integer priority;
}

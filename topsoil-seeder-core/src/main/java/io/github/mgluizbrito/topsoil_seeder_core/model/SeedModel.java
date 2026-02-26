package io.github.mgluizbrito.topsoil_seeder_core.model;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Representation of a block within the YAML seeds file.
 * The YAML structure expects an 'entity' pointing to the fully qualified class
 * name,
 * and an array of 'data', representing rows.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SeedModel {

    private String entity;
    private List<Map<String, Object>> data;
}

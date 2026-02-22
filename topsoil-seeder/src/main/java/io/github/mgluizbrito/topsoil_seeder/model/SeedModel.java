package io.github.mgluizbrito.topsoil_seeder.model;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class SeedModel {

    private String entity;
    private List<Map<String, Object>> data;
}

package io.github.mgluizbrito.topsoil_seeder.yaml;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class YamlParser {

    private static final Yaml yaml = new Yaml();

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> parseYamlFiles(List<InputStream> seeds) throws IOException {
        List<Map<String, Object>> normalizedData = new ArrayList<>();

        for (InputStream seed : seeds) {
            Object loaded = yaml.load(seed);

            // Caso seja o arquivo com múltiplos blocos (- entity: ...)
            if (loaded instanceof List) normalizedData.addAll((List<Map<String, Object>>) loaded);

            // Caso seja o arquivo com um único bloco (entity: ...)
            if (loaded instanceof Map) normalizedData.add((Map<String, Object>) loaded);
        }

        return normalizedData;
    }
}
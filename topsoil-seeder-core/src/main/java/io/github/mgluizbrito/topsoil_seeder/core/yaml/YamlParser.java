package io.github.mgluizbrito.topsoil_seeder.core.yaml;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class responsible for parsing YAML streams into structured Java Maps.
 * It uses the SnakeYAML library under the hood.
 */
public class YamlParser {

    private static final Yaml yaml = new Yaml();

    /**
     * Parses a list of YAML input streams and normalizes them into a single
     * continuous list
     * of maps. Each parsed map represents a top-level entity block in the YAML
     * file.
     *
     * @param seeds A list of {@link InputStream} representing the opened YAML
     *              files.
     * @return A consolidated list of {@link Map} objects containing the parsed YAML
     *         data.
     * @throws IOException if an error occurs while reading the streams.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> parseYamlFiles(List<InputStream> seeds) throws IOException {
        List<Map<String, Object>> normalizedData = new ArrayList<>();

        for (InputStream seed : seeds) {
            Object loaded = yaml.load(seed);

            // If the file contains multiple blocks (e.g. starting with '- entity: ...')
            if (loaded instanceof List)
                normalizedData.addAll((List<Map<String, Object>>) loaded);

            // If the file contains a single block (e.g. starting directly with 'entity:
            // ...')
            if (loaded instanceof Map)
                normalizedData.add((Map<String, Object>) loaded);
        }

        return normalizedData;
    }
}
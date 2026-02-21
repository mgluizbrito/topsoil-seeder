package io.github.mgluizbrito.topsoil_seeder.yaml;

import lombok.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class YamlResourcesScanner {

    public static final String DEFAULT_FOLDER = "seeds";

    /**
     * Searches for .yaml or .yml files in the default 'src/main/resources/seeds' folder
     * @return List of InputStreams ready for SnakeYaml.load()
     */
    public static @NonNull List<InputStream> loadYamlResources() throws IOException, URISyntaxException {
        return loadYamlResources(DEFAULT_FOLDER);
    }

    /**
     * Finds all .yaml files in the specified resources folder.
     * @param resourceFolder The path within src/main/resources/{resourceFolder}
     * @return List of InputStreams ready for SnakeYaml.load()
     */
    public static @NonNull List<InputStream> loadYamlResources(String resourceFolder) throws IOException, URISyntaxException {
        List<InputStream> streams = new ArrayList<InputStream>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String path = resourceFolder.startsWith("/") ? resourceFolder.substring(1) : resourceFolder;
        URL resourceUrl = classLoader.getResource(path);

        if (resourceUrl == null) return Collections.emptyList();
        URI uri = resourceUrl.toURI();

        try (FileSystem fileSystem = getFileSystem(uri)) {
            Path myPath = fileSystem.getPath(path);

            try (Stream<Path> walk = Files.walk(myPath, 1)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                        .forEach(p -> {

                            // usar o classLoader para abrir o stream garante compatibilidade total
                            // O path do NIO pode ter prefixos de sistema que o classloader não entende
                            String resourcePath = path + "/" + p.getFileName().toString();
                            InputStream is = classLoader.getResourceAsStream(resourcePath);
                            if (is != null) streams.add(is);
                        });
            }
        }

        return streams;
    }

    /**
     * Detects if the code is running via file or jar and opens the correct file system.
     * @return Default FileSystem
     */
    public static FileSystem getFileSystem(URI uri) throws IOException {
        if ("jar".equals(uri.getScheme())) {
            try {
                return FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException e) {
                return FileSystems.newFileSystem(uri, Collections.emptyMap());
            }
        }

        return FileSystems.getDefault();
    }
}

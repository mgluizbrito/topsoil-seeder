package io.github.mgluizbrito.topsoil_seeder_core.yaml;

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

/**
 * Scans the classpath for YAML resources to be used as database seeds.
 * Handles reading from both standard file systems during development and JAR
 * archives
 * when the library is packaged.
 */
public class YamlResourcesScanner {

    public static final String DEFAULT_FOLDER = "seeds";

    /**
     * Searches for `.yaml` or `.yml` files in the default
     * 'src/main/resources/seeds' folder.
     *
     * @return A list of {@link InputStream} objects ready for parsing.
     * @throws IOException        if an error occurs scanning or reading files.
     * @throws URISyntaxException if the determined resource URL cannot be mapped to
     *                            a valid URI.
     */
    public static @NonNull List<InputStream> loadYamlResources() throws IOException, URISyntaxException {
        return loadYamlResources(DEFAULT_FOLDER);
    }

    /**
     * Finds all `.yaml` and `.yml` files in the specified resources folder.
     *
     * @param resourceFolder The root path relative to the classpath to scan.
     * @return A list of {@link InputStream} objects ready for parsing.
     * @throws IOException        if an error occurs scanning or reading files.
     * @throws URISyntaxException if the determined resource URL cannot be mapped to
     *                            a valid URI.
     */
    public static @NonNull List<InputStream> loadYamlResources(String resourceFolder) throws IOException, URISyntaxException {
        List<InputStream> streams = new ArrayList<InputStream>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String path = resourceFolder.startsWith("/") ? resourceFolder.substring(1) : resourceFolder;
        URL resourceUrl = classLoader.getResource(path);

        if (resourceUrl == null)
            return Collections.emptyList();
        URI uri = resourceUrl.toURI();

        try {
            FileSystem fileSystem = getFileSystem(uri);

            // To properly support both Jar environments and standard test/dev environments
            // when reading from the local FileSystem, we must use Paths.get(uri) or resolve
            // the absolute path
            Path myPath = "jar".equals(uri.getScheme()) ? fileSystem.getPath(path) : Paths.get(uri);

            try (Stream<Path> walk = Files.walk(myPath, 1)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                        .sorted()
                        .forEach(p -> {

                            // Using the classLoader to open the stream guarantees full compatibility.
                            // The NIO path might carry system prefixes that the classloader does not
                            // understand.
                            String resourcePath = path + "/" + p.getFileName().toString();
                            InputStream is = classLoader.getResourceAsStream(resourcePath);
                            if (is != null) streams.add(is);
                        });
            } finally {
                // IMPORTANT: We must close the file system ONLY if we created a new one for a
                // jar file.
                // Closing the default file system throws an UnsupportedOperationException
                // on some platforms and essentially breaks the JVM's VFS for subsequent uses.
                if ("jar".equals(uri.getScheme()) && fileSystem != FileSystems.getDefault()) {
                    fileSystem.close();
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to load YAML resources from URI: " + uri, e);
        }

        return streams;
    }

    /**
     * Detects if the program is running via an IDE standard filesystem or packaged
     * in a JAR
     * and returns the appropriate FileSystem to walk through.
     *
     * @param uri The URI specifying the root folder layout.
     * @return The appropriate {@link FileSystem}.
     * @throws IOException if custom FileSystem creation fails.
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

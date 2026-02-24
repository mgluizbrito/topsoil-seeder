package io.github.mgluizbrito.topsoil_seeder.yaml;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class YamlParserTest {

    @Test
    void shouldParseSingleEntityBlock() throws IOException {
        // Arrange
        String singleBlockYaml = """
                entity: io.github.mgluizbrito.TestEntity
                data:
                  - _id: user-1
                    name: "Alice"
                  - _id: user-2
                    name: "Bob"
                """;

        InputStream is = new ByteArrayInputStream(singleBlockYaml.getBytes());

        // Act
        List<Map<String, Object>> parsedList = YamlParser.parseYamlFiles(Collections.singletonList(is));

        // Assert
        assertThat(parsedList).hasSize(1);

        Map<String, Object> block = parsedList.get(0);
        assertThat(block).containsEntry("entity", "io.github.mgluizbrito.TestEntity");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> data = (List<Map<String, Object>>) block.get("data");
        assertThat(data).hasSize(2);
        assertThat(data.get(0)).containsEntry("name", "Alice");
        assertThat(data.get(1)).containsEntry("name", "Bob");
    }

    @Test
    void shouldParseMultipleEntityBlocksAsList() throws IOException {
        // Arrange
        String multiBlockYaml = """
                - entity: io.github.mgluizbrito.RoleEntity
                  data:
                    - _id: role-admin
                      name: "Admin"
                - entity: io.github.mgluizbrito.UserEntity
                  data:
                    - _id: user-admin
                      name: "Admin User"
                """;

        InputStream is = new ByteArrayInputStream(multiBlockYaml.getBytes());

        // Act
        List<Map<String, Object>> parsedList = YamlParser.parseYamlFiles(Collections.singletonList(is));

        // Assert
        assertThat(parsedList).hasSize(2);
        assertThat(parsedList.get(0)).containsEntry("entity", "io.github.mgluizbrito.RoleEntity");
        assertThat(parsedList.get(1)).containsEntry("entity", "io.github.mgluizbrito.UserEntity");
    }

    @Test
    void shouldParseEmptyYamlAsEmptyList() throws IOException {
        // Arrange
        InputStream is = new ByteArrayInputStream("".getBytes());

        // Act
        List<Map<String, Object>> parsedList = YamlParser.parseYamlFiles(Collections.singletonList(is));

        // Assert
        assertThat(parsedList).isEmpty();
    }
}

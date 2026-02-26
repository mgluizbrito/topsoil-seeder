package io.github.mgluizbrito.topsoil_seeder.core.mapper;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityMapperTest {

    private final EntityMapper mapper = new EntityMapper();

    @Test
    void shouldMapMapToSimpleEntity() throws ReflectiveOperationException {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("name", "John Doe");
        data.put("age", 30);
        data.put("active", true);

        // Act
        TestEntity entity = mapper.mapToEntity(TestEntity.class, data);

        // Assert
        assertThat(entity.getName()).isEqualTo("John Doe");
        assertThat(entity.getAge()).isEqualTo(30);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void shouldMapMapToEntityWithSuperclassFields() throws ReflectiveOperationException {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("id", 123L); // from superclass
        data.put("name", "Jane Doe");

        // Act
        TestEntity entity = mapper.mapToEntity(TestEntity.class, data);

        // Assert
        assertThat(entity.getId()).isEqualTo(123L);
        assertThat(entity.getName()).isEqualTo("Jane Doe");
    }

    @Test
    void shouldThrowExceptionWhenFieldDoesNotExist() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("nonExistentField", "value");

        // Act & Assert
        assertThatThrownBy(() -> mapper.mapToEntity(TestEntity.class, data))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error defining field 'nonExistentField'");
    }

    // --- Helper Classes ---

    static class BaseEntity {
        private Long id;

        public Long getId() {
            return id;
        }
    }

    static class TestEntity extends BaseEntity {
        private String name;
        private int age;
        private boolean active;

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public boolean isActive() {
            return active;
        }
    }
}

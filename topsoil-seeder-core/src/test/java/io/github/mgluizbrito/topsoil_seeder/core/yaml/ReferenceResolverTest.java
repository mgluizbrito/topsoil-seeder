package io.github.mgluizbrito.topsoil_seeder.core.yaml;

import io.github.mgluizbrito.topsoil_seeder.core.exception.ReferenceNotFound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReferenceResolverTest {

    private ReferenceResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ReferenceResolver();
    }

    @Test
    void shouldResolveSimpleStringReferenceInMap() {
        // Arrange
        resolver.registerReference("role-admin", 100L);

        Map<String, Object> data = new HashMap<>();
        data.put("username", "admin_user");
        data.put("roleId", "@role-admin");

        // Act
        resolver.resolveReferences(data);

        // Assert
        assertThat(data.get("roleId")).isEqualTo(100L);
    }

    @Test
    void shouldResolveDeepNestedReferences() {
        // Arrange
        resolver.registerReference("address-1", 500L);

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> nestedContact = new HashMap<>();
        nestedContact.put("addressId", "@address-1");
        data.put("contactInfo", nestedContact);

        // Act
        resolver.resolveReferences(data);

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> resultNested = (Map<String, Object>) data.get("contactInfo");
        assertThat(resultNested.get("addressId")).isEqualTo(500L);
    }

    @Test
    void shouldResolveReferencesInList() {
        // Arrange
        resolver.registerReference("permission-read", 1L);
        resolver.registerReference("permission-write", 2L);

        Map<String, Object> data = new HashMap<>();
        List<Object> permissions = new ArrayList<>();
        permissions.add("@permission-read");
        permissions.add("@permission-write");
        permissions.add("direct-string-value");
        data.put("permissions", permissions);

        // Act
        resolver.resolveReferences(data);

        // Assert
        @SuppressWarnings("unchecked")
        List<Object> resultList = (List<Object>) data.get("permissions");
        assertThat(resultList).containsExactly(1L, 2L, "direct-string-value");
    }

    @Test
    void shouldThrowExceptionWhenReferenceNotFound() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("roleId", "@missing-role");

        // Act & Assert
        assertThatThrownBy(() -> resolver.resolveReferences(data))
                .isInstanceOf(ReferenceNotFound.class)
                .hasMessageContaining("reference not found: missing-role");
    }

    @Test
    void shouldIgnoreStringsNotStartingWithAtSymbol() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("email", "john@example.com"); // Has '@' but not at index 0

        // Act
        resolver.resolveReferences(data);

        // Assert
        assertThat(data.get("email")).isEqualTo("john@example.com");
    }
}

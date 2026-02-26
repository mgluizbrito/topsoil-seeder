package io.github.mgluizbrito.topsoil_seeder.core.engine;

import io.github.mgluizbrito.topsoil_seeder.core.exception.EntityClassNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeedEngineTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private PersistenceUnitUtil persistenceUnitUtil;

    private SeedEngine engine;

    @BeforeEach
    void setUp() {
        when(entityManager.getTransaction()).thenReturn(transaction);
        engine = new SeedEngine(entityManager);
    }

    @Test
    void shouldSeedSuccessfullyFromValidFolder() {
        // Arrange
        when(entityManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(entityManagerFactory.getPersistenceUnitUtil()).thenReturn(persistenceUnitUtil);
        when(persistenceUnitUtil.getIdentifier(any())).thenReturn(1L, 2L);

        // Act
        engine.seed("test-seeds/valid");

        // Assert
        verify(transaction).begin();
        verify(transaction).commit();

        ArgumentCaptor<Object> entityCaptor = ArgumentCaptor.forClass(Object.class);
        verify(entityManager, times(2)).persist(entityCaptor.capture());
        verify(entityManager, times(2)).flush();

        Object firstEntity = entityCaptor.getAllValues().get(0);
        assertThat(firstEntity).isInstanceOf(SeedEngineTestEntity.class);
        assertThat(((SeedEngineTestEntity) firstEntity).getName()).isEqualTo("First Test Entity");
    }

    @Test
    void shouldRollbackAndThrowExceptionOnMissingEntityClass() {
        // Arrange
        when(transaction.isActive()).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> engine.seed("test-seeds/invalid_class"))
                .isInstanceOf(EntityClassNotFoundException.class)
                .hasMessageContaining("not found");

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(entityManager, never()).persist(any());
    }

    @Test
    void shouldIgnoreEmptyOrInvalidYamlBlocks() {
        // Act
        engine.seed("test-seeds/empty_blocks");

        // Assert
        verify(transaction).begin();
        verify(transaction).commit();
        verify(entityManager, never()).persist(any());
    }

}

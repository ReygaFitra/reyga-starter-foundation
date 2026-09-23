package reyga.starter.foundation.core.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseTransactionalExecutorTest {

    @Test
    void should_SetTransactionalExecutor_When_DependencyIsProvided() {
        // Given
        TransactionalExecutor dependency = mock(TransactionalExecutor.class);
        TestTransactionalExecutor target = new TestTransactionalExecutor();

        // When
        target.setTransactionalExecutor(dependency);

        // Then
        assertSame(dependency, target.getTransactionalExecutor());
        verifyNoInteractions(dependency);
    }

    @Test
    void should_SetNullTransactionalExecutor_When_DependencyIsUnavailable() {
        // Given
        TestTransactionalExecutor target = new TestTransactionalExecutor();

        // When
        target.setTransactionalExecutor(null);

        // Then
        assertNull(target.getTransactionalExecutor());
    }

    private static final class TestTransactionalExecutor extends BaseTransactionalExecutor {
        TransactionalExecutor getTransactionalExecutor() {
            return transactionalExecutor;
        }
    }
}

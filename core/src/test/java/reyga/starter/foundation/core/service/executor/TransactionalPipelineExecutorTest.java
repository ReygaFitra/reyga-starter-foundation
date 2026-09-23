package reyga.starter.foundation.core.service.executor;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionalPipelineExecutorTest {

    private final TransactionalPipelineExecutor executor = new TransactionalPipelineExecutor();

    @Test
    void should_ReturnSupplierResult_When_ExecutionSucceeds() {
        // Given
        @SuppressWarnings("unchecked")
        Supplier<String> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn("result");

        // When
        String result = executor.execute(supplier);

        // Then
        assertEquals("result", result);
        verify(supplier).get();
        verifyNoMoreInteractions(supplier);
    }

    @Test
    void should_PropagateException_When_SupplierFails() {
        // Given
        @SuppressWarnings("unchecked")
        Supplier<String> supplier = mock(Supplier.class);
        IllegalStateException failure = new IllegalStateException("failure");
        when(supplier.get()).thenThrow(failure);

        // When
        IllegalStateException result = assertThrows(IllegalStateException.class, () -> executor.execute(supplier));

        // Then
        assertSame(failure, result);
        verify(supplier).get();
        verifyNoMoreInteractions(supplier);
    }
}

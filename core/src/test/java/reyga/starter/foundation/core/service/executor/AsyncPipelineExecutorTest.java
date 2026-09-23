package reyga.starter.foundation.core.service.executor;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AsyncPipelineExecutorTest {

    private final AsyncPipelineExecutor executor = new AsyncPipelineExecutor();

    @Test
    void should_ReturnSupplierResult_When_AsyncExecutionSucceeds() {
        // Given
        @SuppressWarnings("unchecked")
        Supplier<String> supplier = mock(Supplier.class);
        when(supplier.get()).thenReturn("result");

        // When
        String result = executor.execute(supplier).join();

        // Then
        assertEquals("result", result);
        verify(supplier).get();
        verifyNoMoreInteractions(supplier);
    }

    @Test
    void should_CompleteExceptionally_When_AsyncSupplierFails() {
        // Given
        @SuppressWarnings("unchecked")
        Supplier<String> supplier = mock(Supplier.class);
        IllegalStateException failure = new IllegalStateException("failure");
        when(supplier.get()).thenThrow(failure);

        // When
        CompletionException result = assertThrows(CompletionException.class, () -> executor.execute(supplier).join());

        // Then
        assertSame(failure, result.getCause());
        verify(supplier).get();
        verifyNoMoreInteractions(supplier);
    }

    @Test
    void should_RunTask_When_RunnableIsProvided() {
        // Given
        Runnable task = mock(Runnable.class);

        // When
        executor.execute(task);

        // Then
        verify(task).run();
        verifyNoMoreInteractions(task);
    }

    @Test
    void should_PropagateException_When_RunnableFails() {
        // Given
        Runnable task = mock(Runnable.class);
        IllegalArgumentException failure = new IllegalArgumentException("failure");
        doThrow(failure).when(task).run();

        // When
        IllegalArgumentException result = assertThrows(IllegalArgumentException.class, () -> executor.execute(task));

        // Then
        assertSame(failure, result);
        verify(task).run();
        verifyNoMoreInteractions(task);
    }
}

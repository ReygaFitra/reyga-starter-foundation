package reyga.starter.foundation.core.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefaultTransactionalExecutorTest {

    private PlatformTransactionManager transactionManager;
    private SimpleTransactionStatus transactionStatus;
    private DefaultTransactionalExecutor executor;

    @BeforeEach
    void setUp() {
        transactionManager = mock(PlatformTransactionManager.class);
        transactionStatus = new SimpleTransactionStatus();
        when(transactionManager.getTransaction(any(TransactionDefinition.class)))
                .thenReturn(transactionStatus);
        executor = new DefaultTransactionalExecutor(transactionManager);
    }

    @Test
    void should_ReturnSuppliedValueAndCommit_When_SupplierSucceeds() {
        // given
        @SuppressWarnings("unchecked")
        Supplier<String> supplier = mock(Supplier.class);
        @SuppressWarnings("unchecked")
        Function<Throwable, String> fallback = mock(Function.class);
        when(supplier.get()).thenReturn("success");

        // when
        String result = executor.runInTransaction(supplier, fallback);

        // then
        assertEquals("success", result);
        assertFalse(transactionStatus.isRollbackOnly());
        verify(supplier).get();
        verifyNoInteractions(fallback);
        verify(transactionManager).getTransaction(any(TransactionDefinition.class));
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    void should_ReturnFallbackValueAndMarkRollback_When_SupplierThrowsException() {
        // given
        @SuppressWarnings("unchecked")
        Supplier<String> supplier = mock(Supplier.class);
        @SuppressWarnings("unchecked")
        Function<Throwable, String> fallback = mock(Function.class);
        IllegalStateException exception = new IllegalStateException("failure");
        when(supplier.get()).thenThrow(exception);
        when(fallback.apply(exception)).thenReturn("fallback");

        // when
        String result = executor.runInTransaction(supplier, fallback);

        // then
        assertEquals("fallback", result);
        assertTrue(transactionStatus.isRollbackOnly());
        verify(supplier).get();
        verify(fallback).apply(exception);
        verify(transactionManager).getTransaction(any(TransactionDefinition.class));
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    void should_RunRunnableAndCommitWithoutFallback_When_RunnableSucceeds() {
        // given
        Runnable runnable = mock(Runnable.class);
        @SuppressWarnings("unchecked")
        Consumer<Throwable> fallback = mock(Consumer.class);

        // when
        executor.runInTransaction(runnable, fallback);

        // then
        assertFalse(transactionStatus.isRollbackOnly());
        verify(runnable).run();
        verifyNoInteractions(fallback);
        verify(transactionManager).getTransaction(any(TransactionDefinition.class));
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }

    @Test
    void should_RunFallbackAndMarkRollback_When_RunnableThrowsException() {
        // given
        IllegalStateException expected = new IllegalStateException("failure");
        Runnable runnable = () -> {
            throw expected;
        };
        AtomicInteger fallbackCalls = new AtomicInteger();
        java.util.concurrent.atomic.AtomicReference<Throwable> fallbackArgument =
                new java.util.concurrent.atomic.AtomicReference<>();

        // when
        executor.runInTransaction(runnable, throwable -> {
            fallbackCalls.incrementAndGet();
            fallbackArgument.set(throwable);
        });

        // then
        assertTrue(transactionStatus.isRollbackOnly());
        assertEquals(1, fallbackCalls.get());
        assertSame(expected, fallbackArgument.get());
        verify(transactionManager).getTransaction(any(TransactionDefinition.class));
        verify(transactionManager).commit(transactionStatus);
        verify(transactionManager, never()).rollback(transactionStatus);
    }
}

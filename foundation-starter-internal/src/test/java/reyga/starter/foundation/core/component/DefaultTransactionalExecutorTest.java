package reyga.starter.foundation.core.component;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultTransactionalExecutorTest {

    @Test
    void runInTransaction_returnsValueOnSuccess() {
        PlatformTransactionManager txManager = mockTxManager();
        DefaultTransactionalExecutor executor = new DefaultTransactionalExecutor(txManager);

        String result = executor.runInTransaction(() -> "ok", ex -> "fallback");

        assertEquals("ok", result);
    }

    @Test
    void runInTransaction_returnsFallbackOnException() {
        PlatformTransactionManager txManager = mockTxManager();
        DefaultTransactionalExecutor executor = new DefaultTransactionalExecutor(txManager);

        String result = executor.runInTransaction(() -> {
            throw new IllegalStateException("fail");
        }, ex -> "fallback");

        assertEquals("fallback", result);
    }

    @Test
    void runInTransactionRunnable_callsFallbackOnException() {
        PlatformTransactionManager txManager = mockTxManager();
        DefaultTransactionalExecutor executor = new DefaultTransactionalExecutor(txManager);
        AtomicBoolean fallbackCalled = new AtomicBoolean(false);

        executor.runInTransaction((Runnable) () -> {
            throw new IllegalStateException("fail");
        }, ex -> fallbackCalled.set(true));

        assertTrue(fallbackCalled.get());
    }

    private PlatformTransactionManager mockTxManager() {
        PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);
        TransactionStatus status = new SimpleTransactionStatus();
        when(txManager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
        doNothing().when(txManager).commit(status);
        doNothing().when(txManager).rollback(status);
        return txManager;
    }
}

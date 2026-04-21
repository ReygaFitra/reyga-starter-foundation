package reyga.starter.foundation.core.service.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.component.BaseTransactionalExecutor;
import reyga.starter.foundation.core.component.TransactionalExecutor;
import reyga.starter.foundation.core.validation.BaseValidationProcessor;
import reyga.starter.foundation.core.validation.ValidationUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaseServiceBuilderTest {

    @Mock
    private TransactionalExecutor transactionalExecutor;

    @Mock
    private BaseValidationProcessor validationProcessor;

    private TestableServiceBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new TestableServiceBuilder();
        builder.setTransactionalExecutor(transactionalExecutor);
        ValidationUtility.registerDefault(validationProcessor);
    }

    @Test
    void service_initializesAndClearsState() {
        // First run
        builder.service(new DummyRequest(), new DummyContent())
                .addProcess(() -> {})
                .endProcess(() -> "done")
                .build();
        assertEquals(1, builder.getProcessesCount());

        // Second run should clear previous state
        builder.service(new DummyRequest(), new DummyContent())
                .addProcess(() -> {})
                .addProcess(() -> {})
                .endProcess(() -> "done")
                .build();
        assertEquals(2, builder.getProcessesCount());
    }

    @Test
    void build_executesAllProcessesInOrder() {
        List<String> executionOrder = new ArrayList<>();
        builder.service(new DummyRequest(), new DummyContent())
                .addProcess(() -> executionOrder.add("step1"))
                .addProcess(() -> executionOrder.add("step2"))
                .endProcess(() -> {
                    executionOrder.add("end");
                    return "done";
                })
                .build();

        assertEquals(List.of("step1", "step2", "end"), executionOrder);
    }

    @Test
    void build_withTransactional_wrapsExecution() {
        when(transactionalExecutor.runInTransaction(any(Supplier.class), any()))
                .thenAnswer(invocation -> invocation.getArgument(0, Supplier.class).get());

        builder.service(new DummyRequest(), new DummyContent())
                .withTransactional()
                .endProcess(() -> "done")
                .build();

        verify(transactionalExecutor, times(1)).runInTransaction(any(Supplier.class), any());
    }

    @Test
    void build_withTransactional_usesFallbackOnException() {
        RuntimeException exception = new RuntimeException("DB error");
        when(transactionalExecutor.runInTransaction(any(Supplier.class), any()))
                .thenAnswer(invocation -> {
                    Function<Throwable, String> fallback = invocation.getArgument(1);
                    return fallback.apply(exception);
                });

        String result = builder.service(new DummyRequest(), new DummyContent())
                .withTransactional()
                .onTransactionalFallback(ex -> "fallback_result")
                .addProcess(() -> { throw exception; })
                .endProcess(() -> "should_not_run")
                .build();

        assertEquals("fallback_result", result);
    }
    
    @Test
    void build_withAsync_executesAsynchronously() {
        // This test primarily checks if it runs without error, as true async testing is complex.
        // We ensure the logic completes and returns the expected value.
        AtomicBoolean asyncExecuted = new AtomicBoolean(false);
        String result = builder.service(new DummyRequest(), new DummyContent())
                .withAsync()
                .addProcess(() -> asyncExecuted.set(true))
                .endProcess(() -> "async_done")
                .build();

        assertTrue(asyncExecuted.get());
        assertEquals("async_done", result);
    }

    @Test
    void validateRequest_addsValidationProcess() {
        builder.service(new DummyRequest(), new DummyContent())
                .validateRequest()
                .endProcess(() -> "validated")
                .build();
        
        verify(validationProcessor, times(1)).validateRequest(any(BaseRequest.class), eq(false));
    }

    // --- Helper classes for testing ---

    private static class DummyRequest extends BaseRequest {}
    private static class DummyContent extends BaseContent {}

    private static class TestableServiceBuilder extends BaseServiceBuilder<TestableServiceBuilder, DummyRequest, String, DummyContent> {
        
        @Override
        protected String orchestrate(DummyRequest req, DummyContent content) {
            // Not used directly in these tests, as we call .service() and .build() manually.
            return null;
        }

        @Override
        protected boolean useHttpServletParameter() {
            return false;
        }

        // Expose internal state for testing
        public int getProcessesCount() {
            return super.processes.size();
        }
        
        // Expose protected method for testing
        @Override
        public void setTransactionalExecutor(TransactionalExecutor executor) {
            super.setTransactionalExecutor(executor);
        }
    }
}

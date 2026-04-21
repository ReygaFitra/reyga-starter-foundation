package reyga.starter.foundation.core.service.base;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.service.executor.AsyncPipelineExecutor;
import reyga.starter.foundation.core.service.executor.TransactionalPipelineExecutor;
import reyga.starter.foundation.core.validation.BaseValidationProcessor;
import reyga.starter.foundation.core.validation.ValidationUtility;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineServiceBuilderTest {

    @Mock
    private TransactionalPipelineExecutor transactionalExecutor;
    @Mock
    private AsyncPipelineExecutor asyncExecutor;
    @Mock
    private BaseValidationProcessor validationProcessor;

    private TestablePipelineBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new TestablePipelineBuilder();
        builder.setTransactionalExecutor(transactionalExecutor);
        builder.setAsyncExecutor(asyncExecutor);
        ValidationUtility.registerDefault(validationProcessor);
    }

    @Test
    void pipeline_executesAllStepsInOrder() {
        String result = builder.orchestrate(new DummyRequest(), new DummyContent());

        assertEquals("FINAL: User Reyga ordered Product Laptop", result);
    }

    @Test
    void pipeline_withTransactional_wrapsExecutionInTransaction() {
        when(transactionalExecutor.execute(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Supplier.class).get());

        builder.service(new DummyRequest(), new DummyContent())
                .withTransactional()
                .startPipe(state -> "start")
                .andThen(res -> "end")
                .build();

        verify(transactionalExecutor, times(1)).execute(any(Supplier.class));
    }

    @Test
    void pipeline_withAsync_wrapsExecutionAsynchronously() {
        when(asyncExecutor.execute(any(Supplier.class)))
                .thenReturn(CompletableFuture.completedFuture("async_result"));

        String result = builder.service(new DummyRequest(), new DummyContent())
                .withAsync()
                .startPipe(state -> "start")
                .build();

        verify(asyncExecutor, times(1)).execute(any(Supplier.class));
        assertEquals("async_result", result);
    }

    @Test
    void pipeline_withFallback_handlesException() {
        RuntimeException exception = new IllegalStateException("Insufficient stock");
        when(transactionalExecutor.execute(any(Supplier.class))).thenThrow(exception);

        String result = builder.service(new DummyRequest(), new DummyContent())
                .withTransactional()
                .onTransactionalFallback(ex -> "Fallback: " + ex.getMessage())
                .startPipe(state -> {
                    throw exception; // This will be wrapped by the executor mock
                })
                .build();

        assertEquals("Fallback: Insufficient stock", result);
    }

    @Test
    void doValidationRequest_triggersValidation() {
        builder.service(new DummyRequest(), new DummyContent())
                .startPipe(state -> state)
                .doValidationRequest()
                .build();

        verify(validationProcessor, times(1)).validateRequest(any(BaseRequest.class), eq(false));
    }

    @Test
    void andThenAsync_executesTaskAsynchronously() {
        doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(asyncExecutor).execute(any(Runnable.class));

        Consumer<String> mockConsumer = mock(Consumer.class);

        builder.service(new DummyRequest(), new DummyContent())
                .startPipe(state -> "data_for_async")
                .andThenAsync(mockConsumer)
                .build();

        verify(asyncExecutor, times(1)).execute(any(Runnable.class));
        verify(mockConsumer, times(1)).accept("data_for_async");
    }

    // --- Helper classes for testing ---

    private static class DummyRequest extends BaseRequest {}
    private static class DummyContent extends BaseContent {}
    private record User(String name) {}
    private record Product(String name) {}

    private static class TestablePipelineBuilder extends PipelineServiceBuilder<TestablePipelineBuilder, DummyRequest, String, DummyContent> {

        @Override
        protected String orchestrate(DummyRequest req, DummyContent content) {
            return this.service(req, content)
                    .startPipe(state -> new User("Reyga"))
                    .andThen(user -> {
                        Product product = new Product("Laptop");
                        return "User " + user.name() + " ordered Product " + product.name();
                    })
                    .andThen(res -> "FINAL: " + res)
                    .build();
        }

        @Override
        protected boolean useHttpServletParameter() {
            return false;
        }

        // Expose setters for injecting mocks
        public void setTransactionalExecutor(TransactionalPipelineExecutor executor) {
            this.transactionalExecutor = executor;
        }

        public void setAsyncExecutor(AsyncPipelineExecutor executor) {
            this.asyncExecutor = asyncExecutor;
        }
    }
}

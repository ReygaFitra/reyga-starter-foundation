package reyga.starter.foundation.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common.model.dto.content.BaseContent;
import reyga.starter.foundation.common.model.dto.request.BaseRequest;
import reyga.starter.foundation.core.component.TransactionalExecutor;
import reyga.starter.foundation.core.validation.BaseValidationProcessor;
import reyga.starter.foundation.core.validation.ValidationUtility;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BaseServiceBuilderTest {

    @BeforeEach
    void setUp() {
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        ValidationUtility.registerDefault(processor);
    }

    @Test
    void build_throwsWhenServiceNotInitialized() {
        TestBuilder builder = new TestBuilder();
        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("service(request, content) must be called before build()", ex.getMessage());
    }

    @Test
    void build_throwsWhenEndProcessMissing() {
        NoEndProcessBuilder builder = new NoEndProcessBuilder();
        builder.service(new DummyRequest(), new DummyContent());
        IllegalStateException ex = assertThrows(IllegalStateException.class, builder::build);
        assertEquals("endProcess must be defined before build()", ex.getMessage());
    }

    @Test
    void build_executesProcessesAndEndProcess() {
        TestBuilder builder = new TestBuilder();
        String result = builder.service(new DummyRequest(), new DummyContent()).build();
        assertEquals("done", result);
        assertEquals("ok", builder.getContent().value);
    }

    @Test
    void build_withTransactionalUsesExecutor() {
        TransactionalExecutor executor = mock(TransactionalExecutor.class);
        when(executor.runInTransaction((Supplier<Object>) any(), any())).thenAnswer(invocation -> {
            return invocation.<Supplier<String>>getArgument(0).get();
        });

        TestBuilder builder = new TestBuilder();
        builder.setTransactionalExecutor(executor);
        String result = builder.service(new DummyRequest(), new DummyContent())
                .withTransactional()
                .build();

        assertEquals("done", result);
        verify(executor).runInTransaction((Supplier<Object>) any(), any());
    }

    private static class DummyRequest extends BaseRequest {
    }

    private static class DummyContent extends BaseContent {
        private String value;
    }

    private static class TestBuilder extends BaseServiceBuilder<TestBuilder, DummyRequest, String, DummyContent> {

        @Override
        protected String processFlow(DummyRequest req, DummyContent content) {
            return service(req, content).build();
        }

        @Override
        protected void registerProcesses(ProcessContext<DummyRequest, DummyContent> ctx) {
            ctx.addProcess(() -> ctx.getContent().value = "ok");
            ctx.endProcess(() -> "done");
        }

        public void setTransactionalExecutor(TransactionalExecutor executor) {
            super.setTransactionalExecutor(executor);
        }
    }

    private static class NoEndProcessBuilder extends BaseServiceBuilder<NoEndProcessBuilder, DummyRequest, String, DummyContent> {
        @Override
        protected String processFlow(DummyRequest req, DummyContent content) {
            return service(req, content).build();
        }

        @Override
        protected void registerProcesses(ProcessContext<DummyRequest, DummyContent> ctx) {
            ctx.addProcess(() -> {});
        }
    }
}

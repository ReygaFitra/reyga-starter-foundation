package reyga.starter.foundation.logging.config;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.logging.config.properties.AsyncLoggingProperties;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncLoggingConfigTest {

    @Test
    void should_ReturnMdcAwareExecutor_When_AsyncExecutorBeanIsCreated() {
        // given
        AsyncLoggingConfig config = new AsyncLoggingConfig();
        AsyncLoggingProperties properties = new AsyncLoggingProperties(1, 1, 1, "test-");

        // when
        Executor executor = config.asyncExecutor(properties);

        // then
        assertNotNull(executor);
        assertTrue(executor instanceof MDCAwareExecutor);
    }
}

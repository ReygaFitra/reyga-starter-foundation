package reyga.starter.foundation.logging.config;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.logging.config.properties.AsyncLoggingProperties;

import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncLoggingConfigTest {

    @Test
    void asyncExecutor_returnsMdcAwareExecutor() {
        AsyncLoggingConfig config = new AsyncLoggingConfig();
        AsyncLoggingProperties properties = new AsyncLoggingProperties(1, 1, 1, "test-");

        Executor executor = config.asyncExecutor(properties);

        assertNotNull(executor);
        assertTrue(executor instanceof MDCAwareExecutor);
    }
}

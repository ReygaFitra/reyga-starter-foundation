package reyga.starter.foundation.logging.config;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.logging.service.DefaultLoggingService;
import reyga.starter.foundation.logging.service.LoggingService;

import static org.junit.jupiter.api.Assertions.*;

class LoggingConfigTest {

    @Test
    void should_ReturnDefaultLoggingService_When_LoggingServiceBeanIsCreated() {
        // given
        LoggingConfig config = new LoggingConfig();

        // when
        LoggingService service = config.loggingService();

        // then
        assertNotNull(service);
        assertTrue(service instanceof DefaultLoggingService);
    }
}

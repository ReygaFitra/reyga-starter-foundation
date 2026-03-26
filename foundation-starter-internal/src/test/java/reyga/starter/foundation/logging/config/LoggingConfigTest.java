package reyga.starter.foundation.logging.config;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.logging.service.DefaultLoggingService;
import reyga.starter.foundation.logging.service.LoggingService;

import static org.junit.jupiter.api.Assertions.*;

class LoggingConfigTest {

    @Test
    void loggingService_returnsDefaultLoggingService() {
        LoggingConfig config = new LoggingConfig();
        LoggingService service = config.loggingService();
        assertNotNull(service);
        assertTrue(service instanceof DefaultLoggingService);
    }
}

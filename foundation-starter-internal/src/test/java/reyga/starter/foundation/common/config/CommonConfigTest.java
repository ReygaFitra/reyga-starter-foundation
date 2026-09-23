package reyga.starter.foundation.common.config;

import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.LoggerInjector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonConfigTest {

    private final CommonConfig config = new CommonConfig();

    @Test
    void should_ReturnLoggerInjector_When_LoggerInjectorBeanIsCreated() {
        // given

        // when
        LoggerInjector result = config.loggerInjector();

        // then
        assertNotNull(result);
        assertEquals(LoggerInjector.class, result.getClass());
    }

    @Test
    void should_ReturnCommonLogger_When_LoggerBeanIsCreated() {
        // given

        // when
        CommonLogger result = config.logger();

        // then
        assertNotNull(result);
        assertEquals(CommonLogger.class, result.getClass());
    }
}

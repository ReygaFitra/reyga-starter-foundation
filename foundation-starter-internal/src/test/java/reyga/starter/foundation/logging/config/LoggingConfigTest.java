package reyga.starter.foundation.logging.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import reyga.starter.foundation.logging.service.DefaultLoggingService;
import reyga.starter.foundation.logging.service.LoggingService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

    @Test
    void should_RegisterLoggingService_When_MvcConfigurerIsCreated() {
        LoggingConfig config = new LoggingConfig();
        LoggingService service = mock(LoggingService.class);
        InterceptorRegistry registry = mock(InterceptorRegistry.class);

        LoggingWebMvcConfigurer configurer = config.loggingWebMvcConfigurer(service);
        configurer.addInterceptors(registry);

        verify(registry).addInterceptor(service);
    }
}

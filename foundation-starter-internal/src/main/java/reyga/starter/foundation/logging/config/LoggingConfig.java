package reyga.starter.foundation.logging.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reyga.starter.foundation.logging.config.properties.LoggingProperties;
import reyga.starter.foundation.logging.service.DefaultLoggingService;
import reyga.starter.foundation.logging.service.LoggingService;

/**
 * Configures the optional foundation HTTP logging interceptor.
 */
@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfig {

    /**
     * Creates the logging configuration.
     */
    public LoggingConfig() {
    }

    /**
     * Creates the default logging interceptor when HTTP logging is enabled.
     *
     * @return default Spring MVC logging interceptor
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.logging-handler", havingValue = "true")
    public LoggingService loggingService() {
        return new DefaultLoggingService();
    }

    /**
     * Registers the selected logging service in the Spring MVC interceptor chain.
     *
     * @param loggingService logging interceptor selected by the application context
     * @return MVC configurer that registers the logging interceptor
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.logging-handler", havingValue = "true")
    public LoggingWebMvcConfigurer loggingWebMvcConfigurer(LoggingService loggingService) {
        return new LoggingWebMvcConfigurer(loggingService);
    }

}

package reyga.starter.foundation.core.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import reyga.starter.foundation.core.config.properties.ConfigProperties;
import reyga.starter.foundation.core.exception.handler.DefaultAppValidationExceptionHandler;
import reyga.starter.foundation.core.exception.handler.DefaultExceptionHandler;
import reyga.starter.foundation.core.exception.handler.DefaultValidationExceptionHandler;

/**
 * Configures the ExceptionHandling feature using existing bean names and properties.
 * Explicit legacy CoreConfig imports retain priority to avoid duplicate bean definitions.
 */
@AutoConfiguration
@ConditionalOnMissingBean(CoreConfig.class)
@EnableConfigurationProperties(ConfigProperties.class)
public class ExceptionHandlingAutoConfiguration {

    /** Creates this feature configuration. */
    public ExceptionHandlingAutoConfiguration() {
    }

    /**
     * Creates the existing defaultExceptionHandler bean with unchanged activation and defaults.
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.exception-handler", havingValue = "true")
    public DefaultExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }

    /**
     * Creates the existing defaultAppValidationExceptionHandler bean with unchanged activation and defaults.
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    public DefaultAppValidationExceptionHandler defaultAppValidationExceptionHandler() {
        return new DefaultAppValidationExceptionHandler();
    }

    /**
     * Creates the existing defaultValidationExceptionHandler bean with unchanged activation and defaults.
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.validation-handler", havingValue = "true")
    public DefaultValidationExceptionHandler defaultValidationExceptionHandler() {
        return new DefaultValidationExceptionHandler();
    }

}

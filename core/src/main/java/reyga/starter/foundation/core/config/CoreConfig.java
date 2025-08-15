package reyga.starter.foundation.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reyga.starter.foundation.core.config.properties.ExceptionHandlerProperties;
import reyga.starter.foundation.core.exception.handler.DefaultExceptionHandler;

@Configuration
@Import({ExceptionHandlerProperties.class})
public class CoreConfig {

    @Bean
    @ConditionalOnProperty(name = "reyga.exception-handler.enable-default", havingValue = "true")
    public DefaultExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }

}

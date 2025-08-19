package reyga.starter.foundation.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import reyga.starter.foundation.core.config.properties.ConfigProperties;
import reyga.starter.foundation.core.exception.handler.DefaultExceptionHandler;
import reyga.starter.foundation.core.validation.ValidationProcessor;

@Configuration
@Import({ConfigProperties.class})
public class CoreConfig {

    @Bean
    @ConditionalOnProperty(name = "reyga.config.exception.enable-default", havingValue = "true")
    public DefaultExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.validation.enable-default", havingValue = "true")
    public ValidationProcessor validationProcessor() {
        return new ValidationProcessor();
    }


}

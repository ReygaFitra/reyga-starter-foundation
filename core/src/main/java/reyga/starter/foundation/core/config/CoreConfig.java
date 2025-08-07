package reyga.starter.foundation.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reyga.starter.foundation.core.exception.handler.GlobalExceptionHandler;

@Configuration
public class CoreConfig {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}

package reyga.starter.foundation.logging.config;

import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reyga.starter.foundation.logging.service.LoggingService;

/**
 * Registers the foundation HTTP logging interceptor with Spring MVC.
 */
public class LoggingWebMvcConfigurer implements WebMvcConfigurer {

    private final LoggingService loggingService;

    /**
     * Creates the MVC configurer.
     *
     * @param loggingService interceptor responsible for HTTP execution summaries
     */
    public LoggingWebMvcConfigurer(LoggingService loggingService) {
        this.loggingService = Objects.requireNonNull(
                loggingService,
                "loggingService must not be null"
        );
    }

    /**
     * Adds the logging service to the application's MVC interceptor chain.
     *
     * @param registry active MVC interceptor registry
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(loggingService);
    }
}

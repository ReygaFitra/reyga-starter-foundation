package reyga.starter.foundation.logging.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reyga.starter.foundation.logging.aspect.AspectLogging;
import reyga.starter.foundation.logging.config.properties.LoggingProperties;
import reyga.starter.foundation.logging.interceptor.LogInterceptor;

@Configuration
@RequiredArgsConstructor
@Import(LoggingProperties.class)
public class LogInterceptorConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;

    @Bean
    public LogInterceptor logInterceptor() {
        return logInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.custom.logging.enable-aspect", havingValue = "true")
    public AspectLogging loggingAspect(LoggingProperties loggingProperties) {
        return new AspectLogging(loggingProperties);
    }
}

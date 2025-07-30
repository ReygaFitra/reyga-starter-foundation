package reyga.starter.foundation.logging.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reyga.starter.foundation.common.logging.CustomLogger;
import reyga.starter.foundation.logging.aspect.AspectLogging;
import reyga.starter.foundation.logging.config.properties.LoggingProperties;
import reyga.starter.foundation.logging.interceptor.LogInterceptor;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LoggingProperties.class)
public class LogInterceptorConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;
    private final CustomLogger customLogger;

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
    public AspectLogging loggingAspect() {
        return new AspectLogging(customLogger);
    }
}

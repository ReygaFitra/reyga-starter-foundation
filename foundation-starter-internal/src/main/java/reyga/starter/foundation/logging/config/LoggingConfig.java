package reyga.starter.foundation.logging.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reyga.starter.foundation.logging.aspect.AspectLogging;
import reyga.starter.foundation.logging.aspect.BaseAspectProcessor;
import reyga.starter.foundation.logging.aspect.DefaultAspectProcessor;
import reyga.starter.foundation.logging.config.properties.LoggingProperties;
import reyga.starter.foundation.logging.interceptor.BaseLogInterceptor;
import reyga.starter.foundation.logging.interceptor.DefaultLogInterceptor;

@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfig {

    @Bean
    @ConditionalOnMissingBean(BaseLogInterceptor.class)
    public BaseLogInterceptor logInterceptor() {
        return new DefaultLogInterceptor();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.custom.logging.enable-aspect-logging", havingValue = "true")
    public AspectLogging loggingAspect(LoggingProperties loggingProperties, BaseAspectProcessor baseAspectProcessor) {
        return new AspectLogging(loggingProperties, baseAspectProcessor);
    }

    @Bean
    @ConditionalOnMissingBean(BaseAspectProcessor.class)
    public BaseAspectProcessor defaultAspectProcessor() {
        return new DefaultAspectProcessor();
    }

}

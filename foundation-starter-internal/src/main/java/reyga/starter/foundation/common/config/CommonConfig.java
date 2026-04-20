package reyga.starter.foundation.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.LoggerInjector;

@Configuration
public class CommonConfig {
    @Bean
    public LoggerInjector loggerInjector() {
        return new LoggerInjector();
    }

    @Bean
    public CommonLogger logger() {
        return new CommonLogger(getClass());
    }
}

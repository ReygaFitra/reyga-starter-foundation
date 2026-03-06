package reyga.starter.foundation.logging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reyga.starter.foundation.logging.config.properties.AsyncLoggingProperties;

import java.util.concurrent.Executor;

@Configuration
@Import(AsyncLoggingProperties.class)
public class AsyncLoggingConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor(AsyncLoggingProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setThreadNamePrefix(properties.getThreadName());
        executor.initialize();
        return new MDCAwareExecutor(executor);
    }

}

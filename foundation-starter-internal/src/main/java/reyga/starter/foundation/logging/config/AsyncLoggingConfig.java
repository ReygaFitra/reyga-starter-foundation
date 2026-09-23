package reyga.starter.foundation.logging.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import reyga.starter.foundation.logging.config.properties.AsyncLoggingProperties;

import java.util.concurrent.Executor;

@Configuration
@EnableConfigurationProperties(AsyncLoggingProperties.class)
public class AsyncLoggingConfig {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor(AsyncLoggingProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.corePoolSize());
        executor.setMaxPoolSize(properties.maxPoolSize());
        executor.setQueueCapacity(properties.queueCapacity());
        executor.setThreadNamePrefix(properties.threadName());
        executor.initialize();
        return new MDCAwareExecutor(executor);
    }

}

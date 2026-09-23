package reyga.starter.foundation.logging.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configures the MDC-aware executor used by asynchronous logging tasks.
 *
 * @param corePoolSize minimum number of worker threads
 * @param maxPoolSize maximum number of worker threads
 * @param queueCapacity number of tasks retained before the pool grows
 * @param threadName worker thread name prefix
 */
@ConfigurationProperties(prefix = "reyga.config.logging.file.async-logs")
public record AsyncLoggingProperties(
        @DefaultValue("5") int corePoolSize,
        @DefaultValue("10") int maxPoolSize,
        @DefaultValue("100") int queueCapacity,
        @DefaultValue("async-logging") String threadName
) {
}

package reyga.starter.foundation.logging.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "reyga.custom.logging.async")
public record AsyncLoggingProperties(
        @DefaultValue("5") int corePoolSize,
        @DefaultValue("10") int maxPoolSize,
        @DefaultValue("100") int queueCapacity,
        @DefaultValue("async-logging") String threadName
) {
}

package reyga.starter.foundation.logging.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "reyga.custom.logging.async")
public class AsyncLoggingProperties {
    private int corePoolSize = 5;
    private int maxPoolSize = 10;
    private int queueCapacity = 100;
    private String threadName = "async-logging";
}

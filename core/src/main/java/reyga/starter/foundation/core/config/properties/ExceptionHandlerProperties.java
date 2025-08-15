package reyga.starter.foundation.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "reyga.exception-handler")
public class ExceptionHandlerProperties {
    private boolean enableDefault = true;
}

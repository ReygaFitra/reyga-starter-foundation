package reyga.starter.foundation.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "reyga.config")
public class ConfigProperties {

    @Data
    public static class Exception {
        private boolean enableDefault = true;
    }

    @Data
    public static class Validation {
        private boolean enableDefault = true;
    }

}

package reyga.starter.foundation.logging.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "reyga.custom.logging")
public record LoggingProperties(
        @DefaultValue Console console,
        @DefaultValue Summary summary,
        @DefaultValue Rolling rolling
) {
    public record Console(String pattern) {
    }

    public record Summary(
            @DefaultValue("true") boolean enable,
            String pattern
    ) {
    }

    public record Rolling(
            @DefaultValue("false") boolean enable,
            String pattern,
            String filePath,
            String fileName,
            String summaryFileName,
            Integer maxHistory,
            String maxFileSize
    ) {
    }
}

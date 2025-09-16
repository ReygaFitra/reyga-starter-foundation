package reyga.starter.foundation.logging.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "reyga.custom.logging")
public class LoggingProperties {
    private boolean enableAspectLogging = true;
    private Console console = new Console();
    private Summary summary = new Summary();
    private Rolling rolling = new Rolling();

    @Data
    public static class Console {
        private String pattern;
    }

    @Data
    public static class Summary {
        private boolean enable = true;
        private String pattern;
    }

    @Data
    public static class Rolling {
        private boolean enable = false;
        private String pattern;
        private String filePath;
        private String fileName;
        private String summaryFileName;
        private Integer maxHistory;
        private String maxFileSize;
    }
}

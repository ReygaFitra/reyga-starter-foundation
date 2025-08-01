package reyga.starter.foundation.logging.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "reyga.custom.logging")
public class LoggingProperties {
    private String consolePattern;
    private String filePattern;
    private boolean enableAspect = true;
    private boolean enableSummaryLog = true;
    private boolean enableRollingFile = false;
    private String rollingFile ;
    private String rollingFileName ;
    private String rollingSummaryFileName ;
    private String rollingMaxHistory ;
    private String rollingMaxFileSize ;
}

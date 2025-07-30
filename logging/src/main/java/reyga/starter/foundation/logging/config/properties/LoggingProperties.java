package reyga.starter.foundation.logging.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "reyga.custom.logging")
public class LoggingProperties {
    private String consolePattern = "[%d{yyyy-MM-dd HH:mm:ss.SSS}] x-request-id %-5level %logger{36} - %msg%n";
    private String filePattern = "[%d{yyyy-MM-dd HH:mm:ss.SSS}] | %X{x-request-id} | %-5level | %logger{36} | - %msg%n";
    private boolean enableAspect = true;
    private boolean enableSummaryLog = true;
    private boolean enableRollingFile = false;
    private String rollingFile = "logs/";
    private String rollingFileName = "%d{dd-MM-yyyy}.app.%i.log.gz";
    private String rollingSummaryFileName = "monitoring/%d{dd-MM-yyyy}.summary.%i.log.gz";
    private String rollingMaxHistory = "30";
    private String rollingMaxFileSize = "20MB";
}

package reyga.starter.foundation.logging.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * External configuration contract for console and file log output.
 * File paths may be relative to the process working directory or absolute.
 *
 * @param console console output options
 * @param file active-file, summary, and archive rolling options
 */
@ConfigurationProperties(prefix = "reyga.config.logging")
public record LoggingProperties(
        @DefaultValue Console console,
        @DefaultValue FileLogging file
) {
    /**
     * Configures the console appender pattern.
     *
     * @param pattern Logback pattern for console events
     */
    public record Console(String pattern) {
    }

    /**
     * Configures active log files and their size-and-time-based archives.
     *
     * @param enable whether file logging is enabled
     * @param pattern Logback pattern for regular file events
     * @param filePath base directory for active and archived files
     * @param fileName archive file-name pattern for regular events
     * @param activeFileName active file name for regular events
     * @param cleanHistoryOnStart whether regular archives are cleaned at startup
     * @param summary summary file options
     * @param maxHistory number of archive periods to retain
     * @param maxFileSize maximum archive segment size
     */
    public record FileLogging(
            @DefaultValue("false") boolean enable,
            String pattern,
            String filePath,
            String fileName,
            String activeFileName,
            @DefaultValue("false") boolean cleanHistoryOnStart,
            @DefaultValue Summary summary,
            Integer maxHistory,
            String maxFileSize
    ) {
    }

    /**
     * Configures summary file output and archive cleanup.
     *
     * @param enable whether the summary appender is enabled
     * @param pattern Logback pattern for summary events
     * @param summaryFileName archive file-name pattern for summary events
     * @param summaryActiveFileName active file name for summary events
     * @param summaryCleanHistoryOnStart whether summary archives are cleaned at startup
     */
    public record Summary(
            @DefaultValue("true") boolean enable,
            String pattern,
            String summaryFileName,
            String summaryActiveFileName,
            @DefaultValue("false") boolean summaryCleanHistoryOnStart
    ) {
    }
}

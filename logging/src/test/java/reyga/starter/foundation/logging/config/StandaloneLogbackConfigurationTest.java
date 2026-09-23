package reyga.starter.foundation.logging.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import reyga.starter.foundation.logging.filter.ConsoleLogFilter;
import reyga.starter.foundation.logging.filter.RollingLogFilter;
import reyga.starter.foundation.logging.filter.SummaryLogFilter;
import reyga.starter.foundation.common.enumeration.StarterHeaderEnum;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StandaloneLogbackConfigurationTest {

    @TempDir
    Path logDirectory;

    @ParameterizedTest
    @CsvSource({"false,false", "true,false", "false,true", "true,true"})
    void should_InitializeConfiguredAppenders_When_InternalStarterIsAbsent(
            boolean rolling, boolean summary) {
        // given
        ClassLoader loader = getClass().getClassLoader();
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "reyga.starter.foundation.defaults.config.FoundationDefaultAutoConfiguration",
                false, loader));
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "reyga.config.logging.file.enable", rolling,
                "reyga.config.logging.file.summary.enable", summary,
                "reyga.config.logging.file.file-path",
                logDirectory.toString().replace('\\', '/') + "/"
        )));
        LogbackLoggingSystem system = new LogbackLoggingSystem(loader);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            // when
            system.beforeInitialize();
            system.initialize(new LoggingInitializationContext(environment),
                    "classpath:META-INF/logback-spring.xml", null);

            // then
            Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
            assertAppender(root, "CONSOLE_LOG", true, ConsoleLogFilter.class);
            assertAppender(root, "ROLLING_LOG", rolling, RollingLogFilter.class);
            assertAppender(root, "SUMMARY_LOG", summary, SummaryLogFilter.class);
            assertTrue(context.getStatusManager().getCopyOfStatusList().stream()
                    .noneMatch(status -> status.getLevel() == ch.qos.logback.core.status.Status.ERROR),
                    () -> context.getStatusManager().getCopyOfStatusList().toString());
        } finally {
            system.cleanUp();
            context.reset();
        }
    }

    @Test
    void should_WriteActiveLogFiles_When_RollingAndSummaryAppendersAreEnabled() throws Exception {
        // given
        StandardEnvironment environment = environment(true, true);
        LogbackLoggingSystem system = new LogbackLoggingSystem(getClass().getClassLoader());
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Path rollingFile = logDirectory.resolve("application.log");
        Path summaryFile = logDirectory.resolve("monitoring/summary.log");

        try {
            system.beforeInitialize();
            system.initialize(new LoggingInitializationContext(environment),
                    "classpath:META-INF/logback-spring.xml", null);
            Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);

            // when
            root.info("rolling-file-event");
            MDC.put(StarterHeaderEnum.SUMMARY_LOG.getValue(), "AFTER COMPLETION");
            root.info("summary-file-event");
            MDC.remove(StarterHeaderEnum.SUMMARY_LOG.getValue());

            // then
            assertTrue(Files.isRegularFile(rollingFile));
            assertTrue(Files.readString(rollingFile).contains("rolling-file-event"));
            assertTrue(Files.readString(rollingFile).contains("summary-file-event"));
            assertTrue(Files.isRegularFile(summaryFile));
            assertFalse(Files.readString(summaryFile).contains("rolling-file-event"));
            assertTrue(Files.readString(summaryFile).contains("summary-file-event"));
            assertFalse(rollingPolicy(root, "ROLLING_LOG").isCleanHistoryOnStart());
            assertFalse(rollingPolicy(root, "SUMMARY_LOG").isCleanHistoryOnStart());
            assertNoDeprecatedConfigurationWarnings(context);
        } finally {
            MDC.clear();
            system.cleanUp();
            context.reset();
        }
    }

    @Test
    void should_EnableArchiveCleanup_When_CleanHistoryOptionsAreTrue() {
        // given
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "reyga.config.logging.file.enable", true,
                "reyga.config.logging.file.summary.enable", true,
                "reyga.config.logging.file.clean-history-on-start", true,
                "reyga.config.logging.file.summary.summary-clean-history-on-start", true,
                "reyga.config.logging.file.file-path",
                logDirectory.toString().replace('\\', '/') + "/"
        )));
        LogbackLoggingSystem system = new LogbackLoggingSystem(getClass().getClassLoader());
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            // when
            system.beforeInitialize();
            system.initialize(new LoggingInitializationContext(environment),
                    "classpath:META-INF/logback-spring.xml", null);
            Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);

            // then
            assertTrue(rollingPolicy(root, "ROLLING_LOG").isCleanHistoryOnStart());
            assertTrue(rollingPolicy(root, "SUMMARY_LOG").isCleanHistoryOnStart());
            assertNoDeprecatedConfigurationWarnings(context);
        } finally {
            system.cleanUp();
            context.reset();
        }
    }

    @Test
    void should_NotCreateLogFiles_When_RollingAndSummaryAppendersAreDisabled() {
        // given
        StandardEnvironment environment = environment(false, false);
        LogbackLoggingSystem system = new LogbackLoggingSystem(getClass().getClassLoader());
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            system.beforeInitialize();
            system.initialize(new LoggingInitializationContext(environment),
                    "classpath:META-INF/logback-spring.xml", null);
            Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);

            // when
            root.info("console-only-event");

            // then
            assertFalse(Files.exists(logDirectory.resolve("application.log")));
            assertFalse(Files.exists(logDirectory.resolve("monitoring/summary.log")));
            assertNoDeprecatedConfigurationWarnings(context);
        } finally {
            system.cleanUp();
            context.reset();
        }
    }

    private StandardEnvironment environment(boolean rolling, boolean summary) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "reyga.config.logging.file.enable", rolling,
                "reyga.config.logging.file.summary.enable", summary,
                "reyga.config.logging.file.file-path",
                logDirectory.toString().replace('\\', '/') + "/"
        )));
        return environment;
    }

    private void assertNoDeprecatedConfigurationWarnings(LoggerContext context) {
        assertTrue(context.getStatusManager().getCopyOfStatusList().stream()
                .noneMatch(status -> status.getMessage().contains("condition' attribute")
                        || status.getMessage().contains("Missing watchable")),
                () -> context.getStatusManager().getCopyOfStatusList().toString());
    }

    private TimeBasedRollingPolicy<?> rollingPolicy(Logger root, String appenderName) {
        RollingFileAppender<?> appender = (RollingFileAppender<?>) root.getAppender(appenderName);
        assertNotNull(appender);
        assertInstanceOf(TimeBasedRollingPolicy.class, appender.getRollingPolicy());
        return (TimeBasedRollingPolicy<?>) appender.getRollingPolicy();
    }

    private void assertAppender(Logger root, String name, boolean enabled, Class<?> filterType) {
        Appender<ILoggingEvent> appender = root.getAppender(name);
        if (!enabled) {
            assertNull(appender);
            return;
        }
        assertNotNull(appender);
        assertTrue(appender.isStarted());
        assertEquals(1, appender.getCopyOfAttachedFiltersList().size());
        assertEquals(filterType, appender.getCopyOfAttachedFiltersList().getFirst().getClass());
    }
}

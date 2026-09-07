package reyga.starter.foundation.logging.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import reyga.starter.foundation.logging.filter.ConsoleLogFilter;
import reyga.starter.foundation.logging.filter.RollingLogFilter;
import reyga.starter.foundation.logging.filter.SummaryLogFilter;

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
                "reyga.custom.logging.rolling.enable", rolling,
                "reyga.custom.logging.summary.enable", summary,
                "reyga.custom.logging.rolling.file-path",
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

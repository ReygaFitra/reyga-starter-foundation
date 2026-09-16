package reyga.starter.foundation.logging.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingPropertiesTest {

    private static final String PREFIX = "reyga.config.logging";

    @Test
    void should_ReturnDocumentedDefaults_When_ConfigurationIsEmpty() {
        // given
        Binder binder = new Binder(new MapConfigurationPropertySource());

        // when
        LoggingProperties result = binder.bindOrCreate(
                PREFIX,
                Bindable.of(LoggingProperties.class)
        );

        // then
        assertNotNull(result.console());
        assertNull(result.console().pattern());
        assertNotNull(result.file());
        assertFalse(result.file().enable());
        assertNull(result.file().pattern());
        assertNull(result.file().filePath());
        assertNull(result.file().fileName());
        assertNull(result.file().activeFileName());
        assertFalse(result.file().cleanHistoryOnStart());
        assertNotNull(result.file().summary());
        assertTrue(result.file().summary().enable());
        assertNull(result.file().summary().pattern());
        assertNull(result.file().summary().summaryFileName());
        assertNull(result.file().summary().summaryActiveFileName());
        assertFalse(result.file().summary().summaryCleanHistoryOnStart());
        assertNull(result.file().maxHistory());
        assertNull(result.file().maxFileSize());
    }

    @Test
    void should_ReturnAllConfiguredValues_When_CustomConfigurationIsProvided() {
        // given
        Map<String, Object> properties = new HashMap<>();
        properties.put(PREFIX + ".console.pattern", "console-pattern");
        properties.put(PREFIX + ".file.enable", "true");
        properties.put(PREFIX + ".file.pattern", "file-pattern");
        properties.put(PREFIX + ".file.file-path", "logs/");
        properties.put(PREFIX + ".file.file-name", "application-%d-%i.log.gz");
        properties.put(PREFIX + ".file.active-file-name", "application.log");
        properties.put(PREFIX + ".file.clean-history-on-start", "true");
        properties.put(PREFIX + ".file.summary.enable", "false");
        properties.put(PREFIX + ".file.summary.pattern", "summary-pattern");
        properties.put(PREFIX + ".file.summary.summary-file-name", "summary-%d-%i.log.gz");
        properties.put(PREFIX + ".file.summary.summary-active-file-name", "summary.log");
        properties.put(PREFIX + ".file.summary.summary-clean-history-on-start", "true");
        properties.put(PREFIX + ".file.max-history", "30");
        properties.put(PREFIX + ".file.max-file-size", "20MB");
        Binder binder = new Binder(new MapConfigurationPropertySource(properties));

        // when
        LoggingProperties result = binder.bindOrCreate(
                PREFIX,
                Bindable.of(LoggingProperties.class)
        );

        // then
        assertEquals("console-pattern", result.console().pattern());
        assertTrue(result.file().enable());
        assertEquals("file-pattern", result.file().pattern());
        assertEquals("logs/", result.file().filePath());
        assertEquals("application-%d-%i.log.gz", result.file().fileName());
        assertEquals("application.log", result.file().activeFileName());
        assertTrue(result.file().cleanHistoryOnStart());
        assertFalse(result.file().summary().enable());
        assertEquals("summary-pattern", result.file().summary().pattern());
        assertEquals("summary-%d-%i.log.gz", result.file().summary().summaryFileName());
        assertEquals("summary.log", result.file().summary().summaryActiveFileName());
        assertTrue(result.file().summary().summaryCleanHistoryOnStart());
        assertEquals(30, result.file().maxHistory());
        assertEquals("20MB", result.file().maxFileSize());
    }

    @Test
    void should_ThrowBindException_When_MaxHistoryIsInvalid() {
        // given
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                PREFIX + ".file.max-history", "not-a-number"
        )));

        // when
        BindException result = assertThrows(BindException.class, () ->
                binder.bindOrCreate(PREFIX, Bindable.of(LoggingProperties.class))
        );

        // then
        assertEquals(PREFIX + ".file.max-history", result.getName().toString());
        assertEquals(Integer.class, result.getTarget().getType().resolve());
        assertNotNull(result.getCause());
    }
}

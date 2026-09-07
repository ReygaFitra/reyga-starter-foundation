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

    private static final String PREFIX = "reyga.custom.logging";

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
        assertNotNull(result.summary());
        assertTrue(result.summary().enable());
        assertNull(result.summary().pattern());
        assertNotNull(result.rolling());
        assertFalse(result.rolling().enable());
        assertNull(result.rolling().pattern());
        assertNull(result.rolling().filePath());
        assertNull(result.rolling().fileName());
        assertNull(result.rolling().summaryFileName());
        assertNull(result.rolling().maxHistory());
        assertNull(result.rolling().maxFileSize());
    }

    @Test
    void should_ReturnAllConfiguredValues_When_CustomConfigurationIsProvided() {
        // given
        Map<String, Object> properties = new HashMap<>();
        properties.put(PREFIX + ".console.pattern", "console-pattern");
        properties.put(PREFIX + ".summary.enable", "false");
        properties.put(PREFIX + ".summary.pattern", "summary-pattern");
        properties.put(PREFIX + ".rolling.enable", "true");
        properties.put(PREFIX + ".rolling.pattern", "rolling-pattern");
        properties.put(PREFIX + ".rolling.file-path", "logs/");
        properties.put(PREFIX + ".rolling.file-name", "application.log");
        properties.put(PREFIX + ".rolling.summary-file-name", "summary.log");
        properties.put(PREFIX + ".rolling.max-history", "30");
        properties.put(PREFIX + ".rolling.max-file-size", "20MB");
        Binder binder = new Binder(new MapConfigurationPropertySource(properties));

        // when
        LoggingProperties result = binder.bindOrCreate(
                PREFIX,
                Bindable.of(LoggingProperties.class)
        );

        // then
        assertEquals("console-pattern", result.console().pattern());
        assertFalse(result.summary().enable());
        assertEquals("summary-pattern", result.summary().pattern());
        assertTrue(result.rolling().enable());
        assertEquals("rolling-pattern", result.rolling().pattern());
        assertEquals("logs/", result.rolling().filePath());
        assertEquals("application.log", result.rolling().fileName());
        assertEquals("summary.log", result.rolling().summaryFileName());
        assertEquals(30, result.rolling().maxHistory());
        assertEquals("20MB", result.rolling().maxFileSize());
    }

    @Test
    void should_ThrowBindException_When_MaxHistoryIsInvalid() {
        // given
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                PREFIX + ".rolling.max-history", "not-a-number"
        )));

        // when
        BindException result = assertThrows(BindException.class, () ->
                binder.bindOrCreate(PREFIX, Bindable.of(LoggingProperties.class))
        );

        // then
        assertEquals(PREFIX + ".rolling.max-history", result.getName().toString());
        assertEquals(Integer.class, result.getTarget().getType().resolve());
        assertNotNull(result.getCause());
    }
}

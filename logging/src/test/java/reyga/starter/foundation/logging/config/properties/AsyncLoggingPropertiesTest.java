package reyga.starter.foundation.logging.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AsyncLoggingPropertiesTest {

    private static final String PREFIX = "reyga.custom.logging.async";

    @Test
    void should_ReturnDocumentedDefaults_When_ConfigurationIsEmpty() {
        // given
        Binder binder = new Binder(new MapConfigurationPropertySource());

        // when
        AsyncLoggingProperties result = binder.bindOrCreate(
                PREFIX,
                Bindable.of(AsyncLoggingProperties.class)
        );

        // then
        assertEquals(5, result.corePoolSize());
        assertEquals(10, result.maxPoolSize());
        assertEquals(100, result.queueCapacity());
        assertEquals("async-logging", result.threadName());
    }

    @Test
    void should_ReturnAllConfiguredValues_When_CustomConfigurationIsProvided() {
        // given
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                PREFIX + ".core-pool-size", "2",
                PREFIX + ".max-pool-size", "8",
                PREFIX + ".queue-capacity", "250",
                PREFIX + ".thread-name", "audit-"
        ));
        Binder binder = new Binder(source);

        // when
        AsyncLoggingProperties result = binder.bindOrCreate(
                PREFIX,
                Bindable.of(AsyncLoggingProperties.class)
        );

        // then
        assertEquals(2, result.corePoolSize());
        assertEquals(8, result.maxPoolSize());
        assertEquals(250, result.queueCapacity());
        assertEquals("audit-", result.threadName());
    }

    @Test
    void should_ThrowBindException_When_NumericConfigurationIsInvalid() {
        // given
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
                PREFIX + ".core-pool-size", "not-a-number"
        )));

        // when
        BindException result = assertThrows(BindException.class, () ->
                binder.bindOrCreate(PREFIX, Bindable.of(AsyncLoggingProperties.class))
        );

        // then
        assertEquals(PREFIX + ".core-pool-size", result.getName().toString());
        assertEquals(int.class, result.getTarget().getType().resolve());
        assertNotNull(result.getCause());
    }
}

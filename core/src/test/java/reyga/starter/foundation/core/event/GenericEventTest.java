package reyga.starter.foundation.core.event;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class GenericEventTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-02T03:04:05Z"), ZoneOffset.UTC);

    @Test
    void should_ReturnSourceAndPayload_When_PayloadConstructorIsUsed() {
        // Given
        Object source = new Object();

        // When
        GenericEvent<String> result = new GenericEvent<>(source, "payload");

        // Then
        assertSame(source, result.getSource());
        assertEquals("payload", result.getEvent());
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    void should_ReturnFixedTimestampAndPayload_When_ClockAndPayloadConstructorIsUsed() {
        // Given
        Object source = new Object();

        // When
        GenericEvent<String> result = new GenericEvent<>(source, FIXED_CLOCK, "payload");

        // Then
        assertSame(source, result.getSource());
        assertEquals("payload", result.getEvent());
        assertEquals(FIXED_CLOCK.millis(), result.getTimestamp());
    }

    @Test
    void should_ReturnNullPayload_When_SourceOnlyConstructorIsUsed() {
        // Given
        Object source = new Object();

        // When
        GenericEvent<String> result = new GenericEvent<>(source);

        // Then
        assertSame(source, result.getSource());
        assertNull(result.getEvent());
        assertTrue(result.getTimestamp() > 0);
    }

    @Test
    void should_ReturnFixedTimestampAndNullPayload_When_ClockOnlyConstructorIsUsed() {
        // Given
        Object source = new Object();

        // When
        GenericEvent<String> result = new GenericEvent<>(source, FIXED_CLOCK);

        // Then
        assertSame(source, result.getSource());
        assertNull(result.getEvent());
        assertEquals(FIXED_CLOCK.millis(), result.getTimestamp());
    }
}

package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reyga.starter.foundation.core.annotation.FieldPresence;
import java.util.*;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldPresenceProcessorTest {
    @Test
    void should_CheckOnlyContainerEmptiness_When_CollectionIsProvided() {
        // given
        FieldPresence annotation = mock(FieldPresence.class);
        Collection<?> collection = mock(Collection.class);
        when(collection.isEmpty()).thenReturn(true);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        FieldPresenceProcessor processor = new FieldPresenceProcessor();
        processor.initialize(annotation);
        // when
        boolean result = processor.isValid(collection, context);
        // then
        assertFalse(result);
        verify(collection).isEmpty();
        verifyNoMoreInteractions(collection);
        verifyNoInteractions(context);
        verify(annotation).nullable();
        verify(annotation).allowBlank();
        verifyNoMoreInteractions(annotation);
    }

    @Test
    void should_NotInspectContainer_When_BlankIsAllowed() {
        // given
        FieldPresence annotation = mock(FieldPresence.class);
        when(annotation.allowBlank()).thenReturn(true);
        Collection<?> collection = mock(Collection.class);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        FieldPresenceProcessor processor = new FieldPresenceProcessor();
        processor.initialize(annotation);
        // when
        boolean result = processor.isValid(collection, context);
        // then
        assertTrue(result);
        verifyNoInteractions(collection, context);
        verify(annotation).nullable();
        verify(annotation).allowBlank();
        verifyNoMoreInteractions(annotation);
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void should_ApplyPresencePolicy_When_ValueAndOptionsAreProvided(Object value, boolean nullable,
                                                                  boolean allowBlank, boolean expected) {
        // given
        FieldPresence annotation = mock(FieldPresence.class);
        when(annotation.nullable()).thenReturn(nullable);
        when(annotation.allowBlank()).thenReturn(allowBlank);
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        FieldPresenceProcessor processor = new FieldPresenceProcessor();
        processor.initialize(annotation);
        // when
        boolean result = processor.isValid(value, context);
        // then
        assertEquals(expected, result);
        verify(annotation).nullable();
        verify(annotation).allowBlank();
        verifyNoMoreInteractions(annotation);
        verifyNoInteractions(context);
    }

    static Stream<Arguments> scenarios() {
        List<Object> blanks = Arrays.asList("", " \t\r\n", "\u00a0\u2003",
                new StringBuilder(), new StringBuffer(" "), List.of(), Set.of(), Map.of(),
                new Object[0], new int[0], new byte[0], new boolean[0], new char[0],
                new short[0], new long[0], new float[0], new double[0],
                Optional.empty(), OptionalInt.empty(), OptionalLong.empty(), OptionalDouble.empty());
        List<Object> present = Arrays.asList("x", " x ", new StringBuilder("x"), 0, false,
                java.math.BigDecimal.ZERO, UUID.randomUUID(), java.time.LocalDate.of(2026, 1, 1),
                new Object(), new Object[]{null}, new int[]{0}, new byte[]{0},
                Arrays.asList((Object) null), Map.of("key", ""), Optional.of(""),
                OptionalInt.of(0), OptionalLong.of(0), OptionalDouble.of(0));
        var scenarios = new ArrayList<Arguments>();
        for (boolean nullable : new boolean[]{false, true}) {
            for (boolean blank : new boolean[]{false, true}) {
                scenarios.add(Arguments.of(null, nullable, blank, nullable));
                blanks.forEach(value -> scenarios.add(Arguments.of(value, nullable, blank, blank)));
                present.forEach(value -> scenarios.add(Arguments.of(value, nullable, blank, true)));
            }
        }
        return scenarios.stream();
    }
}

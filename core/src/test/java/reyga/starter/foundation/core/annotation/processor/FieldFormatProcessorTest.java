package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reyga.starter.foundation.common.enumeration.FieldFormatTypeEnum;
import reyga.starter.foundation.core.annotation.FieldFormat;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FieldFormatProcessorTest {

    private FieldFormatProcessor processor;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        processor = new FieldFormatProcessor();
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void should_ReturnFalse_When_ValueTypeIsUnsupported() {
        // Given
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[0], new int[0]);
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid(new Object(), context);

        // Then
        assertFalse(result);
        verifyNoInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnTrue_When_NumberIsAllowed() {
        // Given
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[0], new int[] {1, 2});
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid(2, context);

        // Then
        assertTrue(result);
        verifyNoInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnFalse_When_NumberIsNotAllowed() {
        // Given
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[0], new int[] {1, 2});
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid(3, context);

        // Then
        assertFalse(result);
        verifyViolation("Field field only allows number(s): [1, 2]");
    }

    @Test
    void should_ReturnTrue_When_StringIsAllowed() {
        // Given
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[] {"A", "B"}, new int[0]);
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid(" A ", context);

        // Then
        assertTrue(result);
        verifyNoInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnFalse_When_StringIsNotAllowed() {
        // Given
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[] {"A", "B"}, new int[0]);
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid("C", context);

        // Then
        assertFalse(result);
        verifyViolation("Field field must only contain one of: A, B");
    }

    @Test
    void should_ReturnTrue_When_CustomFormatMatches() {
        // Given
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "^[A-Z]{3}$", new String[0], new int[0]);
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid("ABC", context);

        // Then
        assertTrue(result);
        verifyNoInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnFalse_When_CustomFormatDoesNotMatch() {
        // Given
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "^[A-Z]{3}$", new String[0], new int[0]);
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid("ABCD", context);

        // Then
        assertFalse(result);
        verifyViolation("Invalid Custom Format for field");
    }

    @Test
    void should_ReturnTrue_When_EmailFormatMatches() {
        // Given
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.EMAIL, "", new String[0], new int[0]);
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid("a@b.com", context);

        // Then
        assertTrue(result);
        verifyNoInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnFalse_When_EmailFormatDoesNotMatch() {
        // Given
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.EMAIL, "", new String[0], new int[0]);
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid("invalid", context);

        // Then
        assertFalse(result);
        verifyViolation("Invalid Email Format for field");
    }

    @ParameterizedTest
    @MethodSource("formatScenarios")
    void should_ReturnExpectedValidity_When_FormatTypeInputIsEvaluated(
            FieldFormatTypeEnum formatType, String value, boolean expected, String expectedMessage
    ) {
        // Given
        FieldFormat annotation = mockAnnotation(formatType, "", new String[0], new int[0]);
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertEquals(expected, result);
        if (expected) {
            verifyNoInteractions(context, violationBuilder);
        } else {
            verifyViolation(expectedMessage);
        }
    }

    private static Stream<Arguments> formatScenarios() {
        return Stream.of(
                Arguments.of(FieldFormatTypeEnum.UUID, "0190f21a-7b8c-7def-8123-456789abcdef", true, null),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "999.168.1.1", false, "Invalid IP Address Format for field"),
                Arguments.of(FieldFormatTypeEnum.DATE, "2025-02-29", false, "Invalid Date Format for field"),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "+62 812-345", true, null),
                Arguments.of(FieldFormatTypeEnum.PHONE_NUMBER, "phone", false, "Invalid Phone Number Format for field"),
                Arguments.of(FieldFormatTypeEnum.LETTER_ONLY, "Reyga", true, null),
                Arguments.of(FieldFormatTypeEnum.LETTER_ONLY, "Reyga1", false, "Invalid Letter Format for field"),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "192.168.1.1", true, null),
                Arguments.of(FieldFormatTypeEnum.IP_ADDRESS, "localhost", false, "Invalid IP Address Format for field"),
                Arguments.of(FieldFormatTypeEnum.DATE, "2026-08-26", true, null),
                Arguments.of(FieldFormatTypeEnum.DATE, "26-08-2026", false, "Invalid Date Format for field"),
                Arguments.of(FieldFormatTypeEnum.ALPHANUMERIC, "ABC123", true, null),
                Arguments.of(FieldFormatTypeEnum.ALPHANUMERIC, "ABC-123", false, "Invalid Alphanumeric Format for field"),
                Arguments.of(FieldFormatTypeEnum.NUMERIC_ONLY, "12345", true, null),
                Arguments.of(FieldFormatTypeEnum.NUMERIC_ONLY, "12A", false, "Invalid Number Format for field"),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "Strong1!", true, null),
                Arguments.of(FieldFormatTypeEnum.PASSWORD_STRONG, "weak", false, "Invalid Field Password Format for field"),
                Arguments.of(FieldFormatTypeEnum.UUID, "550e8400-e29b-41d4-a716-446655440000", true, null),
                Arguments.of(FieldFormatTypeEnum.UUID, "invalid-uuid", false, "Invalid Field UUID Format for field"),
                Arguments.of(FieldFormatTypeEnum.ALLOW_ALL, "anything", true, null)
        );
    }

    private void verifyViolation(String message) {
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(message);
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, violationBuilder);
    }

    @Test
    void should_AcceptUuidObject_When_UuidFormatIsSelected() {
        // given
        processor.initialize(mockAnnotation(FieldFormatTypeEnum.UUID, "", new String[0], new int[0]));
        java.util.UUID value = java.util.UUID.fromString("0190f21a-7b8c-7def-8123-456789abcdef");
        // when
        boolean result = processor.isValid(value, context);
        // then
        assertTrue(result);
        verifyNoInteractions(context, violationBuilder);
    }

    @Test
    void should_RejectUuidObject_When_NonUuidFormatIsSelected() {
        // Given
        processor.initialize(mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[0], new int[0]));
        java.util.UUID value = java.util.UUID.fromString("0190f21a-7b8c-7def-8123-456789abcdef");

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertFalse(result);
        verifyViolation("Invalid Format for field");
    }

    @ParameterizedTest
    @MethodSource("invalidNumbers")
    void should_RejectNumberWithoutTruncation_When_NumberIsNotExactlyAllowed(Number value) {
        // given
        processor.initialize(mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[0], new int[]{1}));
        // when
        boolean result = processor.isValid(value, context);
        // then
        assertFalse(result);
        verifyViolation("Field field only allows number(s): [1]");
    }

    static Stream<Number> invalidNumbers() {
        return Stream.of(1.9, 4294967297L, new java.math.BigDecimal("1.00001"), Double.NaN, Double.POSITIVE_INFINITY);
    }

    @Test
    void should_AcceptExactNumericEquivalent_When_NumberIsAllowed() {
        // given
        processor.initialize(mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[0], new int[]{1}));
        // when
        boolean result = processor.isValid(new java.math.BigDecimal("1.0"), context);
        // then
        assertTrue(result);
        verifyNoInteractions(context, violationBuilder);
    }

    @Test
    void should_RejectFraction_When_NumericOnlyFormatIsSelected() {
        // given
        processor.initialize(mockAnnotation(FieldFormatTypeEnum.NUMERIC_ONLY, "", new String[0], new int[0]));
        // when
        boolean result = processor.isValid(1.5, context);
        // then
        assertFalse(result);
        verifyViolation("Invalid Number Format for field");
    }

    @Test
    void should_ApplyCustomPatternToNumber_When_CustomFormatIsConfigured() {
        // given
        processor.initialize(mockAnnotation(FieldFormatTypeEnum.EMAIL, "[0-9]{3}", new String[0], new int[0]));
        // when
        boolean result = processor.isValid(123, context);
        // then
        assertTrue(result);
        verifyNoInteractions(context, violationBuilder);
    }

    @Test
    void should_ApplyFormatToNumbers_When_EmailFormatIsSelected() {
        // given
        processor.initialize(mockAnnotation(FieldFormatTypeEnum.EMAIL, "", new String[0], new int[0]));
        // when
        boolean result = processor.isValid(123, context);
        // then
        assertFalse(result);
        verifyViolation("Invalid Email Format for field");
    }

    @Test
    void should_UseCustomMessage_When_FormatFails() {
        // given
        var annotation = mockAnnotation(FieldFormatTypeEnum.UUID, "", new String[0], new int[0]);
        when(annotation.message()).thenReturn("invalid identifier");
        processor.initialize(annotation);
        // when
        boolean result = processor.isValid("invalid", context);
        // then
        assertFalse(result);
        verifyViolation("invalid identifier");
    }

    @Test
    void should_RejectMalformedConfiguration_When_CustomRegexIsInvalid() {
        // given
        var annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "[", new String[0], new int[0]);
        // when
        var error = assertThrows(java.util.regex.PatternSyntaxException.class, () -> processor.initialize(annotation));
        // then
        assertEquals("[", error.getPattern());
        verifyNoInteractions(context, violationBuilder);
    }

    @Test
    void should_RejectNullAndUnsupportedObjects_When_ValueCannotBeFormatted() {
        // given
        processor.initialize(mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[0], new int[0]));
        // when
        boolean nullResult = processor.isValid(null, context);
        boolean objectResult = processor.isValid(new Object(), context);
        // then
        assertFalse(nullResult);
        assertFalse(objectResult);
        verifyNoInteractions(context, violationBuilder);
    }

    private FieldFormat mockAnnotation(FieldFormatTypeEnum type, String customFormat, String[] onlyContains, int[] onlyNumbers) {
        FieldFormat annotation = mock(FieldFormat.class);
        when(annotation.message()).thenReturn("");
        when(annotation.fieldName()).thenReturn("field");
        when(annotation.formatType()).thenReturn(type);
        when(annotation.customFormat()).thenReturn(customFormat);
        when(annotation.onlyContains()).thenReturn(onlyContains);
        when(annotation.onlyNumbers()).thenReturn(onlyNumbers);
        return annotation;
    }
}

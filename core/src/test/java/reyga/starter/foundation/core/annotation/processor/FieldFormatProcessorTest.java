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

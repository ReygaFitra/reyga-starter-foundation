package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.core.annotation.FieldLength;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FieldLengthProcessorTest {

    private FieldLengthProcessor processor;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        processor = new FieldLengthProcessor();
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
        FieldLength annotation = mock(FieldLength.class);
        when(annotation.message()).thenReturn("");
        when(annotation.fieldName()).thenReturn("field");
        when(annotation.min()).thenReturn(2L);
        when(annotation.max()).thenReturn(4L);
        processor.initialize(annotation);
    }

    @Test
    void should_ReturnTrue_When_ValueIsNull() {
        // Given
        Object value = null;

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void should_ReturnFalse_When_ValueTypeIsUnsupported() {
        // Given
        Object value = new Object();

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("field field must be number or string");
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnTrue_When_NumberLengthIsWithinRange() {
        // Given
        int value = 1234;

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void should_ReturnFalse_When_NumberLengthIsBelowMinimum() {
        // Given
        int value = 1;

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("field field minimum digit length 2");
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnFalse_When_NumberLengthIsAboveMaximum() {
        // Given
        int value = 12345;

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("field field maximum digit length 4");
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnTrue_When_StringLengthIsWithinRange() {
        // Given
        String value = "abcd";

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void should_ReturnFalse_When_StringLengthIsOutsideRange() {
        // Given
        String value = "abcde";

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("FIELD field maximum length 4 characters");
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, violationBuilder);
    }
}

package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.core.annotation.FieldNumberRange;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FieldNumberRangeProcessorTest {

    private FieldNumberRangeProcessor processor;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        processor = new FieldNumberRangeProcessor();
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation()).thenReturn(context);
        FieldNumberRange annotation = mock(FieldNumberRange.class);
        when(annotation.message()).thenReturn("");
        when(annotation.fieldName()).thenReturn("field");
        when(annotation.min()).thenReturn(2L);
        when(annotation.max()).thenReturn(4L);
        processor.initialize(annotation);
    }

    @Test
    void should_ReturnTrue_When_ValueIsNull() {
        // Given
        Number value = null;

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void should_ReturnFalse_When_ValueIsBelowMinimum() {
        // Given
        int value = 1;

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("field value must be >= 2");
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnFalse_When_ValueIsAboveMaximum() {
        // Given
        int value = 5;

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("field value must be <= 4");
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, violationBuilder);
    }

    @Test
    void should_ReturnTrue_When_ValueIsWithinRange() {
        // Given
        int value = 3;

        // When
        boolean result = processor.isValid(value, context);

        // Then
        assertTrue(result);
        verifyNoInteractions(context);
    }

    @Test
    void should_ReturnFalseWithCustomMessage_When_ValueIsOutsideRange() {
        // Given
        FieldNumberRange annotation = mock(FieldNumberRange.class);
        when(annotation.message()).thenReturn("custom range error");
        when(annotation.fieldName()).thenReturn("amount");
        when(annotation.min()).thenReturn(10L);
        when(annotation.max()).thenReturn(20L);
        processor.initialize(annotation);

        // When
        boolean result = processor.isValid(9, context);

        // Then
        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("custom range error");
        verify(violationBuilder).addConstraintViolation();
        verifyNoMoreInteractions(context, violationBuilder);
    }
}

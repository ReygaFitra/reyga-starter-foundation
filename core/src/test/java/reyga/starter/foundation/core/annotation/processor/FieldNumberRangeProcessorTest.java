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

    @BeforeEach
    void setUp() {
        processor = new FieldNumberRangeProcessor();
        context = mockContext();
        FieldNumberRange annotation = mock(FieldNumberRange.class);
        when(annotation.message()).thenReturn("");
        when(annotation.fieldName()).thenReturn("field");
        when(annotation.min()).thenReturn(2L);
        when(annotation.max()).thenReturn(4L);
        processor.initialize(annotation);
    }

    @Test
    void isValid_returnsTrue_forNull() {
        assertTrue(processor.isValid(null, context));
    }

    @Test
    void isValid_returnsFalse_whenBelowMin() {
        assertFalse(processor.isValid(1, context));
        verify(context).buildConstraintViolationWithTemplate("field value must be >= 2");
    }

    @Test
    void isValid_returnsFalse_whenAboveMax() {
        assertFalse(processor.isValid(5, context));
        verify(context).buildConstraintViolationWithTemplate("field value must be <= 4");
    }

    @Test
    void isValid_returnsTrue_whenWithinRange() {
        assertTrue(processor.isValid(3, context));
    }

    private ConstraintValidatorContext mockContext() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
        return context;
    }
}

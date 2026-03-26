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

    @BeforeEach
    void setUp() {
        processor = new FieldLengthProcessor();
        context = mockContext();
        FieldLength annotation = mock(FieldLength.class);
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
    void isValid_returnsFalse_forUnsupportedType() {
        assertFalse(processor.isValid(new Object(), context));
        verify(context).buildConstraintViolationWithTemplate("field field must be number or string");
    }

    @Test
    void isValid_validatesNumberLength() {
        assertFalse(processor.isValid(1, context));
        assertTrue(processor.isValid(1234, context));
        assertFalse(processor.isValid(12345, context));
    }

    @Test
    void isValid_validatesStringLength() {
        assertFalse(processor.isValid("a", context));
        assertTrue(processor.isValid("abcd", context));
        assertFalse(processor.isValid("abcde", context));
    }

    private ConstraintValidatorContext mockContext() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
        return context;
    }
}

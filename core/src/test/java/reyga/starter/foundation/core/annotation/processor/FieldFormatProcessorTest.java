package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.common.enumeration.FieldFormatTypeEnum;
import reyga.starter.foundation.core.annotation.FieldFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FieldFormatProcessorTest {

    private FieldFormatProcessor processor;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        processor = new FieldFormatProcessor();
        context = mockContext();
    }

    @Test
    void isValid_returnsFalse_forUnsupportedType() {
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[0], new int[0]);
        processor.initialize(annotation);

        assertFalse(processor.isValid(new Object(), context));
    }

    @Test
    void isValid_validatesOnlyNumbers_forNumber() {
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[0], new int[] {1, 2});
        processor.initialize(annotation);

        assertTrue(processor.isValid(2, context));
        assertFalse(processor.isValid(3, context));
    }

    @Test
    void isValid_validatesOnlyContains_forString() {
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "", new String[] {"A", "B"}, new int[0]);
        processor.initialize(annotation);

        assertTrue(processor.isValid("A", context));
        assertFalse(processor.isValid("C", context));
    }

    @Test
    void isValid_usesCustomFormat_whenProvided() {
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.ALLOW_ALL, "^[A-Z]{3}$", new String[0], new int[0]);
        processor.initialize(annotation);

        assertTrue(processor.isValid("ABC", context));
        assertFalse(processor.isValid("ABCD", context));
    }

    @Test
    void isValid_usesFormatType_whenNoCustomFormat() {
        FieldFormat annotation = mockAnnotation(FieldFormatTypeEnum.EMAIL, "", new String[0], new int[0]);
        processor.initialize(annotation);

        assertTrue(processor.isValid("a@b.com", context));
        assertFalse(processor.isValid("invalid", context));
    }

    private ConstraintValidatorContext mockContext() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
        return context;
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

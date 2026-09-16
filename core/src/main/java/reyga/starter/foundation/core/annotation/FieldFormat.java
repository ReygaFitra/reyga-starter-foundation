package reyga.starter.foundation.core.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import reyga.starter.foundation.common.enumeration.FieldFormatTypeEnum;
import reyga.starter.foundation.core.annotation.processor.FieldFormatProcessor;

import java.lang.annotation.*;

/**
 * Validates String and Number fields, plus UUID fields when {@link
 * FieldFormatTypeEnum#UUID} is selected. A UUID object is rejected for every other
 * format type. Null and unsupported types are invalid. customFormat replaces the
 * built-in format for String and Number values; allow-lists still apply. Numeric
 * allow-lists compare exact numeric values (without int truncation). Built-in text
 * checks do not trim. DATE requires a real ISO calendar date; IP_ADDRESS accepts IPv4 only.
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldFormatProcessor.class)
public @interface FieldFormat {
    String message() default "";
    String fieldName() default "";
    FieldFormatTypeEnum formatType() default FieldFormatTypeEnum.ALLOW_ALL;
    String customFormat() default "";
    String[] onlyContains() default {};
    int[] onlyNumbers() default {};
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

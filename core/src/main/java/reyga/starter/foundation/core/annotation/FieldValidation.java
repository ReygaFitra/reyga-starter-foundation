package reyga.starter.foundation.core.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import reyga.starter.foundation.common.enumeration.FieldFormatTypeEnum;
import reyga.starter.foundation.core.annotation.processor.FieldValidationProcessor;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldValidationProcessor.class)
public @interface FieldValidation {
    String message() default "";
    String fieldName() default "";
    FieldFormatTypeEnum formatType() default FieldFormatTypeEnum.ALLOW_ALL;
    String customFormat() default "";
    String[] onlyContains() default {};
    int[] onlyNumbers() default {};
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
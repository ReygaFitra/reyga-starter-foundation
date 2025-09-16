package reyga.starter.foundation.core.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import reyga.starter.foundation.core.annotation.processor.FieldNumberRangeProcessor;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {FieldNumberRangeProcessor.class})
public @interface FieldNumberRange {
    String message() default "";
    String fieldName() default "";
    long min() default Long.MIN_VALUE;
    long max() default Long.MAX_VALUE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

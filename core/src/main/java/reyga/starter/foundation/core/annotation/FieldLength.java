package reyga.starter.foundation.core.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import reyga.starter.foundation.core.annotation.processor.FieldLengthProcessor;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldLengthProcessor.class)
public @interface FieldLength {
    String message() default "";
    String fieldName() default "";
    long min() default 0;
    long max() default Long.MAX_VALUE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

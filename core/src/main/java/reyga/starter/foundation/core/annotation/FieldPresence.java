package reyga.starter.foundation.core.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import reyga.starter.foundation.core.annotation.processor.FieldPresenceProcessor;
import java.lang.annotation.*;

/**
 * Controls nullability and emptiness independently for any Java value.
 * Blank means empty/Unicode-whitespace CharSequence, empty Collection/Map/array,
 * or empty Optional (including primitive Optional variants). Other non-null values
 * are present, including zero, false and arbitrary objects. Container contents are
 * not traversed. Combine with other constraints or Valid for element validation.
 * Other constraints retain their own null/blank rules.
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldPresenceProcessor.class)
public @interface FieldPresence {
    /** @return whether a null reference is accepted, independently of allowBlank */
    boolean nullable() default false;

    /** @return whether an empty value is accepted; never controls null references */
    boolean allowBlank() default false;

    /** @return Jakarta-interpolated validation message */
    String message() default "must satisfy nullable={nullable} and allowBlank={allowBlank}";

    /** @return validation groups */
    Class<?>[] groups() default {};

    /** @return Jakarta validation payload metadata */
    Class<? extends Payload>[] payload() default {};
}

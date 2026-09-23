package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import reyga.starter.foundation.core.annotation.FieldPresence;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/** Checks presence without coercing values to strings or traversing container contents. */
public final class FieldPresenceProcessor implements ConstraintValidator<FieldPresence, Object> {
    private boolean nullable;
    private boolean allowBlank;

    /** Creates a validator for Jakarta Bean Validation. */
    public FieldPresenceProcessor() {}

    /** Reads immutable annotation options.
     * @param annotation presence constraint
     */
    @Override
    public void initialize(FieldPresence annotation) {
        nullable = annotation.nullable();
        allowBlank = annotation.allowBlank();
    }

    /** Checks null references first, then emptiness for supported containers and text.
     * Uses the annotation message; does not replace the default violation.
     * @param value any Java value (primitive fields are boxed by Jakarta Validation)
     * @param context validation context, not modified
     * @return whether the configured presence policy accepts the value
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return nullable;
        if (allowBlank) return true;
        if (value instanceof CharSequence text) {
            return text.codePoints().anyMatch(cp -> !Character.isWhitespace(cp) && !Character.isSpaceChar(cp));
        }
        if (value instanceof Collection<?> collection) return !collection.isEmpty();
        if (value instanceof Map<?, ?> map) return !map.isEmpty();
        if (value.getClass().isArray()) return Array.getLength(value) != 0;
        if (value instanceof Optional<?> optional) return optional.isPresent();
        if (value instanceof OptionalInt optional) return optional.isPresent();
        if (value instanceof OptionalLong optional) return optional.isPresent();
        if (value instanceof OptionalDouble optional) return optional.isPresent();
        return true;
    }
}

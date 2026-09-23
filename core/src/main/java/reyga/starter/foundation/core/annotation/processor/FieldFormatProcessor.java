package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import reyga.starter.foundation.common.enumeration.FieldFormatTypeEnum;
import reyga.starter.foundation.core.annotation.FieldFormat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Applies allow-lists and a built-in or custom format to strings and numbers. UUID
 * objects are supported exclusively with {@link FieldFormatTypeEnum#UUID}; their
 * canonical textual representation is then checked. Null and unsupported object types
 * are invalid. A custom regex replaces the built-in format, but never the allow-lists.
 * No request value is retained between validations.
 */
public class FieldFormatProcessor implements ConstraintValidator<FieldFormat, Object> {
    private String message;
    private String fieldName;
    private FieldFormatTypeEnum formatType;
    private Pattern customPattern;
    private List<String> onlyContains;
    private int[] onlyNumbers;

    /** Caches annotation options and compiles a non-blank custom regex once.
     * @param annotation field constraint
     * @throws java.util.regex.PatternSyntaxException if the custom expression is malformed
     */
    @Override
    public void initialize(FieldFormat annotation) {
        message = annotation.message();
        fieldName = annotation.fieldName();
        formatType = annotation.formatType();
        String custom = annotation.customFormat();
        customPattern = custom == null || custom.isBlank() ? null : Pattern.compile(custom);
        onlyContains = List.of(annotation.onlyContains());
        onlyNumbers = annotation.onlyNumbers().clone();
    }

    /**
     * Checks a value without numeric truncation or format bypass.
     * @param value String, Number, or a UUID when the UUID format is selected; null and
     *              other combinations are invalid
     * @param context Jakarta validation context for error details
     * @return true when allow-lists and selected format accept the value
     */
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value instanceof UUID uuid) return validateUuid(uuid, context);
        if (!(value instanceof String) && !(value instanceof Number)) return false;
        if (value instanceof Number number && !numberAllowed(number)) {
            return reject(context, "Field " + fieldName + " only allows number(s): " + Arrays.toString(onlyNumbers));
        }
        String text = value.toString();
        if (!onlyContains.isEmpty() && !onlyContains.contains(text.trim())) {
            return reject(context, "Field " + fieldName + " must only contain one of: " + String.join(", ", onlyContains));
        }
        if (customPattern != null) {
            return customPattern.matcher(text).matches() || reject(context, "Invalid Custom Format for " + fieldName);
        }
        return formatType.matches(text) || reject(context, formatMessage() + " " + fieldName);
    }

    private boolean validateUuid(UUID value, ConstraintValidatorContext context) {
        return formatType == FieldFormatTypeEnum.UUID && formatType.matches(value.toString())
                || reject(context, formatMessage() + " " + fieldName);
    }

    private boolean numberAllowed(Number value) {
        if (onlyNumbers.length == 0) return true;
        try {
            BigDecimal number = new BigDecimal(value.toString());
            return Arrays.stream(onlyNumbers).anyMatch(allowed -> number.compareTo(BigDecimal.valueOf(allowed)) == 0);
        } catch (NumberFormatException error) {
            return false;
        }
    }

    private String formatMessage() {
        return switch (formatType) {
            case EMAIL -> "Invalid Email Format for";
            case PHONE_NUMBER -> "Invalid Phone Number Format for";
            case LETTER_ONLY -> "Invalid Letter Format for";
            case IP_ADDRESS -> "Invalid IP Address Format for";
            case DATE -> "Invalid Date Format for";
            case ALPHANUMERIC -> "Invalid Alphanumeric Format for";
            case NUMERIC_ONLY -> "Invalid Number Format for";
            case PASSWORD_STRONG -> "Invalid Field Password Format for";
            case UUID -> "Invalid Field UUID Format for";
            case ALLOW_ALL -> "Invalid Format for";
        };
    }

    private boolean reject(ConstraintValidatorContext context, String defaultMessage) {
        String selected = message == null || message.isBlank() ? defaultMessage : message;
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(selected).addConstraintViolation();
        return false;
    }
}

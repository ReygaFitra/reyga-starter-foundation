package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import reyga.starter.foundation.core.annotation.FieldNumberRange;

public class FieldNumberRangeProcessor implements ConstraintValidator<FieldNumberRange, Number> {

    private String message;
    private String fieldName;
    private long min;
    private long max;

    @Override
    public void initialize(FieldNumberRange constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.fieldName = constraintAnnotation.fieldName();
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) return true;

        long longValue = value.longValue();
        String finalMessage = this.message;

        if (longValue < min) {
            if (finalMessage.isBlank()) {
                finalMessage = String.format("%s value must be >= %d", fieldName, min);
            }
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(finalMessage).addConstraintViolation();
            return false;
        }

        if (longValue > max) {
            if (finalMessage.isBlank()) {
                finalMessage = String.format("%s value must be <= %d", fieldName, max);
            }
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(finalMessage).addConstraintViolation();
            return false;
        }

        return true;
    }
}

package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import reyga.starter.foundation.core.annotation.FieldLength;

public class FieldLengthProcessor implements ConstraintValidator<FieldLength, Object> {

    private String message;
    private String fieldName;
    private long min;
    private long max;

    @Override
    public void initialize(FieldLength constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.fieldName = constraintAnnotation.fieldName();
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) return true;

        if (!(value instanceof String) && !(value instanceof Number)) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(String.format("%s field must be number or string", this.fieldName))
                    .addConstraintViolation();
            return false;
        }

        if (value instanceof Number) {
            return handleNumberLengthValidation(constraintValidatorContext, (Number) value);
        }

        return handleStringLengthValidation(constraintValidatorContext, (String) value);
    }

    private boolean handleNumberLengthValidation(ConstraintValidatorContext context, Number numVal) {
        String message;
        long length = String.valueOf(Math.abs(numVal.longValue())).length();

        if (length < this.min) {
            message = (this.message == null || this.message.isBlank()) ? String.format(
                    "%s field minimum digit length %d", this.fieldName, this.min) : this.message;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        if (length > this.max) {
            message = (this.message == null || this.message.isBlank()) ? String.format(
                    "%s field maximum digit length %d", this.fieldName, this.max) : this.message;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean handleStringLengthValidation(ConstraintValidatorContext context, String strVal) {
        String message;

        if (strVal != null && strVal.length() < this.min) {
            message = (this.message == null || this.message.isBlank()) ? String.format(
                    "%S field minimum length %d characters", this.fieldName, this.min) : this.message;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        if (strVal != null && strVal.length() > this.max) {
            message = (this.message == null || this.message.isBlank()) ? String.format(
                    "%S field maximum length %d characters", this.fieldName, this.max) : this.message;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        return true;
    }

}

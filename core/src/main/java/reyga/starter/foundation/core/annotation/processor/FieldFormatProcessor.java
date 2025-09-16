package reyga.starter.foundation.core.annotation.processor;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import reyga.starter.foundation.common.enumeration.FieldFormatTypeEnum;
import reyga.starter.foundation.core.annotation.FieldFormat;

import java.util.Arrays;
import java.util.List;

public class FieldFormatProcessor implements ConstraintValidator<FieldFormat, Object> {

    private String message;
    private String fieldName;
    private FieldFormatTypeEnum formatType;
    private String customFormat;
    private List<String> onlyContains;
    private int[] onlyNumbers;

    @Override
    public void initialize(FieldFormat constraintAnnotation) {
        this.message = constraintAnnotation.message();
        this.fieldName = constraintAnnotation.fieldName();
        this.formatType = constraintAnnotation.formatType();
        this.customFormat = constraintAnnotation.customFormat();
        this.onlyContains = List.of(constraintAnnotation.onlyContains());
        this.onlyNumbers = constraintAnnotation.onlyNumbers();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (!(value instanceof String) && !(value instanceof Number)) {
            return false;
        }

        if (value instanceof Number numVal) {
            return constructOnlyNumberValidProcess(context, numVal.intValue());
        }

        String strVal = (String) value;
        if (!constructOnlyContainsFieldValidProcess(context, strVal)) {
            return false;
        }

        if (this.customFormat != null && !this.customFormat.isBlank()) {
            return constructFormatTypeFieldValidProcess(context, strVal, this.customFormat, "Invalid Custom Format for");
        }

        return handleFormatTypeValidation(strVal, context);
    }

    private boolean handleFormatTypeValidation(String strVal, ConstraintValidatorContext context) {
        return switch (this.formatType) {
            case EMAIL -> constructFormatTypeFieldValidProcess(context, strVal,
                    FieldFormatTypeEnum.EMAIL.getRegex(), "Invalid Email Format for");
            case PHONE_NUMBER -> constructFormatTypeFieldValidProcess(context, strVal,
                    FieldFormatTypeEnum.PHONE_NUMBER.getRegex(), "Invalid Phone Number Format for");
            case LETTER_ONLY -> constructFormatTypeFieldValidProcess(context, strVal,
                    FieldFormatTypeEnum.LETTER_ONLY.getRegex(), "Invalid Letter Format for");
            case IP_ADDRESS -> constructFormatTypeFieldValidProcess(context, strVal,
                    FieldFormatTypeEnum.IP_ADDRESS.getRegex(), "Invalid IP Address Format for");
            case DATE -> constructFormatTypeFieldValidProcess(context, strVal,
                    FieldFormatTypeEnum.DATE.getRegex(), "Invalid Date Format for");
            case ALPHANUMERIC -> constructFormatTypeFieldValidProcess(context, strVal,
                    FieldFormatTypeEnum.ALPHANUMERIC.getRegex(), "Invalid Alphanumeric Format for");
            case NUMERIC_ONLY -> constructFormatTypeFieldValidProcess(context, strVal,
                    FieldFormatTypeEnum.NUMERIC_ONLY.getRegex(), "Invalid Number Format for");
            case PASSWORD_STRONG -> constructFormatTypeFieldValidProcess(context, strVal,
                    FieldFormatTypeEnum.PASSWORD_STRONG.getRegex(), "Invalid Field Password Format for");
            case UUID -> constructFormatTypeFieldValidProcess(context, strVal,
                    FieldFormatTypeEnum.UUID.getRegex(), "Invalid Field UUID Format for");
            case ALLOW_ALL -> true;
        };
    }

    private boolean constructOnlyContainsFieldValidProcess(ConstraintValidatorContext context, String strVal) {
        if (this.onlyContains == null || this.onlyContains.isEmpty()) {
            return true;
        }

        boolean matched = this.onlyContains.stream().anyMatch(opt -> opt.equals(strVal.trim()));
        if (!matched) {
            String msg = (this.message == null || this.message.isBlank())
                    ? "Field " + this.fieldName + " must only contain one of: " + String.join(", ", this.onlyContains)
                    : this.message;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean constructFormatTypeFieldValidProcess(ConstraintValidatorContext context, String value, String regex, String defaultMsg) {
        if (value != null && !value.matches(regex)) {
            String msg = (this.message == null || this.message.isBlank()) ? defaultMsg + " " + this.fieldName : this.message;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
        return true;
    }

    private boolean constructOnlyNumberValidProcess(ConstraintValidatorContext context, int value) {
        if (this.onlyNumbers.length == 0) return true;

        boolean match = Arrays.stream(this.onlyNumbers).anyMatch(allowed -> allowed == value);
        if (!match) {
            String msg = (this.message == null || this.message.isBlank())
                    ? "Field " + fieldName + " only allows number(s): " + Arrays.toString(this.onlyNumbers)
                    : this.message;
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
        return true;
    }


}

package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;
import reyga.starter.foundation.common.util.DateUtility;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.ValidationFaultException;

import java.util.Collection;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static reyga.starter.foundation.core.exception.AppFaultContent.buildAppFaultContent;

public class ValidationProcessor extends BaseValidationProcessor {

    private static final String VALIDATION_BUSINESS = "Validation Exception";
    private static final String VALIDATION_ADDITIONAL_INFO = "One or more request fields are invalid.";

    /** Creates a processor using the legacy default validation bootstrap. */
    public ValidationProcessor() { super(); }

    /** Creates a processor with a caller-managed validator.
     * @param validator non-null Jakarta validator
     */
    public ValidationProcessor(Validator validator) { super(validator); }

    @Override
    public <T> void violationsSetHandle(Set<ConstraintViolation<T>> violations) {
        if (!violations.isEmpty()) {
            Set<String> validationMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            throwErrorWithLog(validationMessage, toFieldErrorDetails(violations));
        }
    }

    @Override
    public <T> void violationsMapHandle(Set<ConstraintViolation<T>> violations) {
        if (!violations.isEmpty()) {
            List<Map<String, Object>> validationMessage = violations.stream()
                    .map(this::toViolationMap)
                    .toList();
            throwErrorWithLog(validationMessage, toFieldErrorDetails(violations));
        }
    }

    private <T> Map<String, Object> toViolationMap(ConstraintViolation<T> violation) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("field", violation.getPropertyPath().toString());
        detail.put("message", violation.getMessage());
        detail.put("rejectedValue", safeToString(violation.getInvalidValue()));
        detail.put("constraint", violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName());
        detail.put("rootBean", violation.getRootBeanClass().getSimpleName());
        return detail;
    }

    private String safeToString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        int max = 200;
        if (text.length() <= max) {
            return text;
        }
        return text.substring(0, max) + "...(truncated)";
    }

    private <T> List<FieldErrorDetail> toFieldErrorDetails(Set<ConstraintViolation<T>> violations) {
        var timestamp = DateUtility.getTimestamp(LocalDateTime.now(ZoneId.systemDefault()));
        return violations.stream()
                .map(violation -> FieldErrorDetail.builder()
                        .field(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .timestamp(timestamp)
                        .build())
                .sorted(Comparator.comparing(FieldErrorDetail::getField, Comparator.nullsFirst(String::compareTo))
                        .thenComparing(FieldErrorDetail::getMessage, Comparator.nullsFirst(String::compareTo)))
                .toList();
    }

    private void throwErrorWithLog(Object valueMsg, List<FieldErrorDetail> fieldErrorList) {
        if (logger != null && valueMsg instanceof Collection<?> collection) {
            logger.warn("Validation Error Count", collection.size());
        }
        if (logger != null) logger.warn("Validation Error Detail", valueMsg);
        AppFaultContent faultContent = buildAppFaultContent(
                VALIDATION_BUSINESS, ServiceCodeEnum.VALIDATION_ERROR.getCode(), ServiceCodeEnum.VALIDATION_ERROR.getMessage(),
                valueMsg, HttpStatus.BAD_REQUEST
        );
        throw new ValidationFaultException(
                faultContent, VALIDATION_BUSINESS, VALIDATION_ADDITIONAL_INFO, fieldErrorList
        );
    }

}

package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static reyga.starter.foundation.core.exception.AppFaultContent.buildAppFaultContent;

public class ValidationProcessor extends BaseValidationProcessor {

    @InjectLogger
    protected CommonLogger logger;

    @Override
    public <T> void violationsSetHandle(Set<ConstraintViolation<T>> violations) throws AppFaultException {
        if (!violations.isEmpty()) {
            Set<String> validationMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            throwErrorWithLog(validationMessage);
        }
    }

    @Override
    public <T> void violationsMapHandle(Set<ConstraintViolation<T>> violations) throws AppFaultException {
        if (!violations.isEmpty()) {
            List<Map<String, Object>> validationMessage = violations.stream()
                    .map(this::toViolationMap)
                    .toList();
            throwErrorWithLog(validationMessage);
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

    private void throwErrorWithLog(Object valueMsg) throws AppFaultException {
        if (valueMsg instanceof Collection<?> collection) {
            logger.warn("Validation Error Count", collection.size());
        }
        logger.warn("Validation Error Detail", valueMsg);
        AppFaultContent faultContent = buildAppFaultContent(
                "Validation Exception", "01", "Invalid Request", valueMsg, HttpStatus.BAD_REQUEST
        );
        throw new AppFaultException(faultContent);
    }

}

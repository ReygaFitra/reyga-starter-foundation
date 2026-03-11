package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static reyga.starter.foundation.core.exception.AppFaultContent.buildAppFaultContent;

public class ValidationProcessor extends BaseValidationProcessor {

    @Override
    public <T> void violationsSetHandle(Set<ConstraintViolation<T>> violations) throws AppFaultException {
        if (!violations.isEmpty()) {
            Set<String> validationMessage = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            throwErrorWithLog(validationMessage);
        }
    }

    @Override
    public <T> void violationsMapHandle(Set<ConstraintViolation<T>> violations) throws AppFaultException {
        if (!violations.isEmpty()) {
            List<Map<String, String>> validationMessage = violations
                    .stream()
                    .map(errors -> new HashMap<String, String>() {{
                        put(
                                errors.getPropertyPath().toString().concat(" "),
                                " ".concat(errors.getMessage())
                        );
                            }}
                    )
                    .collect(Collectors.toList());
            throwErrorWithLog(validationMessage);
        }
    }

    private void throwErrorWithLog(Object valueMsg) throws AppFaultException {
        log.warn("Validation Message", valueMsg);
        AppFaultContent faultContent = buildAppFaultContent(
                "Validation Exception", "01", "Invalid Request", valueMsg, HttpStatus.BAD_REQUEST
        );
        throw new AppFaultException(faultContent);
    }

}

package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reyga.starter.foundation.common.exception.AppFaultContent;
import reyga.starter.foundation.common.exception.AppFaultException;
import reyga.starter.foundation.common.logging.CustomLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static reyga.starter.foundation.common.util.ServiceUtil.buildAppFaultContent;

@Component
@RequiredArgsConstructor
public class ValidationProcessor {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();
    private final CustomLogger logger;

    private void throwErrorWithLog(String infoMsg, Object valueMsg) throws AppFaultException {
        logger.warn(infoMsg, valueMsg);
        AppFaultContent faultContent = buildAppFaultContent(
                "Validation Exception", "01", "Invalid Request", valueMsg, HttpStatus.BAD_REQUEST
        );
        throw new AppFaultException(faultContent);
    }

    public <T> void validateRequest(T request, boolean useMapPattern) throws AppFaultException {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (useMapPattern) {
            violationsMapHandle(violations);
        } else {
            violationsSetHandle(violations);
        }
    }

    @SafeVarargs
    public final <T, GT> void validateRequest(T request, boolean useMapPattern, Class<GT>... validationGroup) throws AppFaultException {
        Set<ConstraintViolation<T>> violations = validator.validate(request, validationGroup);
        if (useMapPattern) {
            violationsMapHandle(violations);
        } else {
            violationsSetHandle(violations);
        }
    }

    private <T> void violationsSetHandle(Set<ConstraintViolation<T>> violations) throws AppFaultException {
        if (!violations.isEmpty()) {
            Set<String> validationMessage = violations
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.toSet());
            throwErrorWithLog("Validation Message", validationMessage);
        }
    }

    private <T> void violationsMapHandle(Set<ConstraintViolation<T>> violations) throws AppFaultException {
        if (!violations.isEmpty()) {
            List<Map<String, String>> validationMessage = violations
                    .stream()
                    .map(errors -> new HashMap<String, String>() {{
                                put(errors.getMessage().concat(" "), " ".concat(errors.getInvalidValue().toString()));
                            }}
                    )
                    .collect(Collectors.toList());
            throwErrorWithLog("Validation Message", validationMessage);
        }
    }
}

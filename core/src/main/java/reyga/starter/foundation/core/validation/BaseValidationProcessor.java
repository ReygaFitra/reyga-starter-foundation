package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import reyga.starter.foundation.common.logging.BaseLogging;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public abstract class BaseValidationProcessor extends BaseLogging {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    protected <T> void validateRequest(T request, boolean useMapPattern) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (useMapPattern) {
            violationsMapHandle(violations);
        } else {
            violationsSetHandle(violations);
        }
    };

    protected <T, GT> void validateRequest(T request, boolean useMapPattern, List<Class<GT>> validationGroups) {
        Class<?>[] valGroupArr = validationGroups.toArray(new Class<?>[0]);
        Set<ConstraintViolation<T>> violations = validator.validate(request, valGroupArr);
        if (useMapPattern) {
            violationsMapHandle(violations);
        } else {
            violationsSetHandle(violations);
        }
    };

    protected abstract <T> void violationsSetHandle(Set<ConstraintViolation<T>> violations);
    protected abstract <T> void violationsMapHandle(Set<ConstraintViolation<T>> violations);

}

package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;

import java.util.List;
import java.util.Set;

public abstract class BaseValidationProcessor {

    @InjectLogger
    protected CommonLogger logger;

    private final Validator validator;

    protected BaseValidationProcessor() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        } catch (Exception e) {
            if (logger != null) {
                logger.error("Error Occurred in ValidatorFactory: ", e.getMessage());
            }
            throw new IllegalArgumentException(e);
        }
    }

    public <T> void validateRequest(T request, boolean useMapPattern) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (useMapPattern) {
            violationsMapHandle(violations);
        } else {
            violationsSetHandle(violations);
        }
    }

    public <T, G> void validateRequest(T request, boolean useMapPattern, List<Class<G>> validationGroups) {
        Class<?>[] valGroupArr = validationGroups.toArray(new Class<?>[0]);
        Set<ConstraintViolation<T>> violations = validator.validate(request, valGroupArr);
        if (useMapPattern) {
            violationsMapHandle(violations);
        } else {
            violationsSetHandle(violations);
        }
    }

    protected abstract <T> void violationsSetHandle(Set<ConstraintViolation<T>> violations);
    protected abstract <T> void violationsMapHandle(Set<ConstraintViolation<T>> violations);

}

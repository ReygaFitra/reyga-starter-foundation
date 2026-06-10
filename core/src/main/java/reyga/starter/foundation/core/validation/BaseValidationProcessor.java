package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;

import java.util.List;
import java.util.Set;

/**
 * Base class for processing Jakarta Bean Validation.
 * Provides core logic for validating request objects and handling constraint violations.
 */
public abstract class BaseValidationProcessor {

    @InjectLogger
    protected CommonLogger logger;

    private final Validator validator;

    /**
     * Initializes the Validator using the default ValidatorFactory.
     */
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

    /**
     * Validates the given request object.
     *
     * @param request       the object to validate
     * @param useMapPattern if true, uses {@link #violationsMapHandle}, otherwise uses {@link #violationsSetHandle}
     * @param <T>           the type of the request object
     */
    public <T> void validateRequest(T request, boolean useMapPattern) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (useMapPattern) {
            violationsMapHandle(violations);
        } else {
            violationsSetHandle(violations);
        }
    }

    /**
     * Validates the given request object against specific validation groups.
     *
     * @param request          the object to validate
     * @param useMapPattern    if true, uses {@link #violationsMapHandle}, otherwise uses {@link #violationsSetHandle}
     * @param validationGroups the list of validation groups to apply
     * @param <T>              the type of the request object
     * @param <G>              the type of the validation groups
     */
    public <T, G> void validateRequest(T request, boolean useMapPattern, List<Class<G>> validationGroups) {
        Class<?>[] valGroupArr = validationGroups.toArray(new Class<?>[0]);
        Set<ConstraintViolation<T>> violations = validator.validate(request, valGroupArr);
        if (useMapPattern) {
            violationsMapHandle(violations);
        } else {
            violationsSetHandle(violations);
        }
    }

    /**
     * Handles validation violations using a set-based approach.
     *
     * @param violations the set of constraint violations
     * @param <T>        the type of the validated object
     */
    protected abstract <T> void violationsSetHandle(Set<ConstraintViolation<T>> violations);

    /**
     * Handles validation violations using a map-based approach.
     *
     * @param violations the set of constraint violations
     * @param <T>        the type of the validated object
     */
    protected abstract <T> void violationsMapHandle(Set<ConstraintViolation<T>> violations);

}

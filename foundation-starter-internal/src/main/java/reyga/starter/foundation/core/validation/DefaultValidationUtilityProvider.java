package reyga.starter.foundation.core.validation;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.Objects;

/** Runtime factory retaining owned ValidatorFactory resources until the utility is closed. */
public final class DefaultValidationUtilityProvider implements ValidationUtilityProvider {
    /** Creates the provider used by ServiceLoader. */
    public DefaultValidationUtilityProvider() {}

    /** {@inheritDoc} */
    @Override
    public ValidationUtility create(ValidationConfig config) {
        Objects.requireNonNull(config, "config must not be null");
        if (config.processor() != null) return new DefaultValidationUtility(config, config.processor(), null);
        if (config.validator() != null) {
            return new DefaultValidationUtility(config, new ValidationProcessor(config.validator()), null);
        }
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        try {
            return new DefaultValidationUtility(config, new ValidationProcessor(factory.getValidator()), factory);
        } catch (RuntimeException | Error error) {
            factory.close();
            throw error;
        }
    }
}

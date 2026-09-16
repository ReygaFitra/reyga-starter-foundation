package reyga.starter.foundation.core.validation;

/** Runtime SPI for creating validation utilities; applications normally use ValidationConfig. */
public interface ValidationUtilityProvider {
    /**
     * Creates a new utility without modifying shared state.
     * @param config immutable, non-null configuration
     * @return non-null utility
     */
    ValidationUtility create(ValidationConfig config);
}

package reyga.starter.foundation.core.validation;

import jakarta.validation.Validator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Immutable validation options created by a mutable, non-thread-safe builder.
 * Build once per application bean and inject the resulting ValidationUtility.
 * No Spring context or global registration is required by this API.
 */
public final class ValidationConfig {
    private final Validator validator;
    private final BaseValidationProcessor processor;
    private final boolean mapDetails;
    private final List<Class<?>> groups;

    private ValidationConfig(Builder builder) {
        validator = builder.validator;
        processor = builder.processor;
        mapDetails = builder.mapDetails;
        groups = List.copyOf(builder.groups);
    }

    /** @return a fresh builder using default Jakarta validation and map details */
    public static Builder builder() { return new Builder(); }
    /** @return caller-owned validator, or null to use the default provider */
    public Validator validator() { return validator; }
    /** @return caller-owned processor, or null to use standard error handling */
    public BaseValidationProcessor processor() { return processor; }
    /** @return true for field-detail maps, false for a set of messages */
    public boolean mapDetails() { return mapDetails; }
    /** @return immutable default validation groups; empty selects Jakarta Default */
    public List<Class<?>> groups() { return groups; }

    /** Configures independent utilities; later changes do not affect previously built instances. */
    public static final class Builder {
        private Validator validator;
        private BaseValidationProcessor processor;
        private boolean mapDetails = true;
        private List<Class<?>> groups = List.of();

        private Builder() {}

        /** Resets all options to default validation, map details, and Default groups.
         * @return this builder
         */
        public Builder useDefaultBehavior() {
            validator = null;
            processor = null;
            mapDetails = true;
            groups = List.of();
            return this;
        }

        /** Uses a caller-owned validator with standard error handling; replaces a custom processor.
         * @param validator non-null Jakarta validator, never closed by this utility
         * @return this builder
         * @throws NullPointerException if validator is null
         */
        public Builder withValidator(Validator validator) {
            this.validator = Objects.requireNonNull(validator, "validator must not be null");
            processor = null;
            return this;
        }

        /** Uses caller-owned validation and error handling; replaces a custom validator.
         * @param processor non-null processor, never closed by this utility
         * @return this builder
         * @throws NullPointerException if processor is null
         */
        public Builder withProcessor(BaseValidationProcessor processor) {
            this.processor = Objects.requireNonNull(processor, "processor must not be null");
            validator = null;
            return this;
        }

        /** Selects field-detail maps (including rejected values) for constraint errors.
         * @return this builder
         */
        public Builder useMapDetails() { mapDetails = true; return this; }

        /** Selects unique error messages without rejected values.
         * @return this builder
         */
        public Builder useMessageDetails() { mapDetails = false; return this; }

        /** Sets default groups; method-level groups replace these, not append to them.
         * @param groups non-null group interfaces; empty selects Jakarta Default
         * @return this builder
         * @throws NullPointerException if array or any group is null
         * @throws IllegalArgumentException if a group is not an interface
         */
        public Builder withValidationGroups(Class<?>... groups) {
            Objects.requireNonNull(groups, "groups must not be null");
            for (Class<?> group : groups) {
                Objects.requireNonNull(group, "group must not be null");
                if (!group.isInterface()) throw new IllegalArgumentException("validation groups must be interfaces");
            }
            this.groups = List.of(groups.clone());
            return this;
        }

        /** Builds an independent utility using the runtime provider.
         * @return closeable validation utility; Spring closes a managed bean automatically
         * @throws IllegalStateException if no provider or multiple providers are available
         */
        public ValidationUtility build() {
            var providers = ServiceLoader.load(ValidationUtilityProvider.class).iterator();
            if (!providers.hasNext()) {
                throw new IllegalStateException("Validation provider not found. Add foundation-starter to the runtime classpath.");
            }
            ValidationUtilityProvider provider = providers.next();
            if (providers.hasNext()) throw new IllegalStateException("Multiple validation providers found");
            return Objects.requireNonNull(provider.create(new ValidationConfig(this)), "provider returned null");
        }
    }
}

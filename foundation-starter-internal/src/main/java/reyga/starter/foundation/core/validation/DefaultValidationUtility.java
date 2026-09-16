package reyga.starter.foundation.core.validation;

import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instance-scoped validation facade. Does not retain requests or mutate default groups.
 * Custom processors must support the concurrency required by the application.
 * Close only after all in-flight validation calls have completed.
 */
final class DefaultValidationUtility implements ValidationUtility {
    private final ValidationConfig config;
    private final BaseValidationProcessor processor;
    private final ValidatorFactory ownedFactory;
    private final AtomicBoolean closed = new AtomicBoolean();

    DefaultValidationUtility(ValidationConfig config, BaseValidationProcessor processor, ValidatorFactory ownedFactory) {
        this.config = config;
        this.processor = processor;
        this.ownedFactory = ownedFactory;
    }

    @Override
    public ValidationUtility validateRequest(Object request) {
        validateRequest(request, config.mapDetails(), config.groups());
        return this;
    }

    @Override
    public ValidationUtility validateRequest(Object request, List<? extends Class<?>> groups) {
        validateRequest(request, config.mapDetails(), groups);
        return this;
    }

    @Override
    public <T> void validateRequest(T request, boolean useMapPattern) {
        validateRequest(request, useMapPattern, config.groups());
    }

    @Override
    public <T> void validateRequest(T request, boolean useMapPattern, List<? extends Class<?>> groups) {
        if (closed.get()) throw new IllegalStateException("ValidationUtility is closed");
        Objects.requireNonNull(request, "request must not be null");
        Objects.requireNonNull(groups, "groups must not be null");
        List<Class<?>> snapshot = List.copyOf(groups);
        for (Class<?> group : snapshot) {
            if (!group.isInterface()) throw new IllegalArgumentException("validation groups must be interfaces");
        }
        if (snapshot.isEmpty()) processor.validateRequest(request, useMapPattern);
        else processor.validateRequest(request, useMapPattern, snapshot);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true) && ownedFactory != null) ownedFactory.close();
    }
}

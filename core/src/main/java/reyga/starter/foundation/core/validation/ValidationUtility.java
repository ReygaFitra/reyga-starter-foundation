package reyga.starter.foundation.core.validation;

import java.util.List;

public final class ValidationUtility {

    private static ValidationUtility defaultInstance;

    private final BaseValidationProcessor processor;

    private ValidationUtility(BaseValidationProcessor processor) {
        this.processor = processor;
    }

    public static void registerDefault(BaseValidationProcessor processor) {
        defaultInstance = new ValidationUtility(processor);
    }

    public static ValidationUtility chain() {
        ValidationUtility utility = defaultInstance;
        if (utility == null) {
            throw new IllegalStateException(
                    "ValidationUtility bean is not available. Ensure reyga.config.default-bean.validation-handler=true"
            );
        }
        return utility;
    }

    public ValidationUtility validateRequest(Object request) {
        processor.validateRequest(request, true);
        return this;
    }

    public <G> ValidationUtility validateRequest(Object request, List<Class<G>> validationGroups) {
        processor.validateRequest(request, true, validationGroups);
        return this;
    }

    public <T> void validateRequest(T request, boolean useMapPattern) {
        processor.validateRequest(request, useMapPattern);
    }

    public <T, G> void validateRequest(T request, boolean useMapPattern, List<Class<G>> validationGroups) {
        processor.validateRequest(request, useMapPattern, validationGroups);
    }

}

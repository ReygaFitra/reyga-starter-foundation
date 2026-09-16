package reyga.starter.foundation.core.validation;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationUtilityTest {
    @Test
    void should_ExplainRuntimeDependency_When_ProviderIsUnavailable() {
        // given
        var builder = ValidationConfig.builder().useDefaultBehavior();
        // when
        var error = assertThrows(IllegalStateException.class, builder::build);
        // then
        assertEquals("Validation provider not found. Add foundation-starter to the runtime classpath.", error.getMessage());
        assertTrue(ValidationUtility.class.isInterface());
    }

    @Test
    void should_RejectNullOptions_When_BuilderArgumentsAreNull() {
        // given
        var builder = ValidationConfig.builder();
        // when
        var validator = assertThrows(NullPointerException.class, () -> builder.withValidator(null));
        var processor = assertThrows(NullPointerException.class, () -> builder.withProcessor(null));
        var groups = assertThrows(NullPointerException.class, () -> builder.withValidationGroups((Class<?>[]) null));
        var group = assertThrows(NullPointerException.class, () -> builder.withValidationGroups((Class<?>) null));
        // then
        assertEquals("validator must not be null", validator.getMessage());
        assertEquals("processor must not be null", processor.getMessage());
        assertEquals("groups must not be null", groups.getMessage());
        assertEquals("group must not be null", group.getMessage());
    }

    @Test
    void should_RejectNonInterfaceGroups_When_GroupIsAClass() {
        // given
        var builder = ValidationConfig.builder();
        // when
        var error = assertThrows(IllegalArgumentException.class, () -> builder.withValidationGroups(String.class));
        // then
        assertEquals("validation groups must be interfaces", error.getMessage());
    }

    @Test
    void should_ReturnBuilderWithoutUsingDependencies_When_OptionsAreConfigured() {
        // given
        var builder = ValidationConfig.builder();
        Validator validator = mock(Validator.class);
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        // when
        var result = builder.withValidator(validator).withProcessor(processor)
                .useMessageDetails().useMapDetails().withValidationGroups(Runnable.class).useDefaultBehavior();
        // then
        assertSame(builder, result);
        verifyNoInteractions(validator, processor);
    }
}

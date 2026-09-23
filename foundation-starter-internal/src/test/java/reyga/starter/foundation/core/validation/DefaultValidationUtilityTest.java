package reyga.starter.foundation.core.validation;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.core.exception.ValidationFaultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultValidationUtilityTest {
    record FormattedRequest(
            @reyga.starter.foundation.core.annotation.FieldFormat(
                    formatType = reyga.starter.foundation.common.enumeration.FieldFormatTypeEnum.UUID)
            java.util.UUID id,
            @reyga.starter.foundation.core.annotation.FieldFormat(
                    fieldName = "date", formatType = reyga.starter.foundation.common.enumeration.FieldFormatTypeEnum.DATE)
            String date) {}

    @Test
    void should_ValidateFormatsThroughUtility_When_UuidObjectAndCalendarDateAreProvided() {
        // given
        var id = java.util.UUID.fromString("0190f21a-7b8c-7def-8123-456789abcdef");
        try (var utility = ValidationConfig.builder().useDefaultBehavior().build()) {
            // when
            var result = utility.validateRequest(new FormattedRequest(id, "2024-02-29"));
            var fault = assertThrows(ValidationFaultException.class,
                    () -> utility.validateRequest(new FormattedRequest(id, "2025-02-29")));
            // then
            assertSame(utility, result);
            assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), fault.getFaultContent().getErrorCode());
            assertEquals("Invalid Request", fault.getFaultContent().getErrorMessage());
            assertEquals(HttpStatus.BAD_REQUEST, fault.getFaultContent().getStatusCode());
            assertEquals(List.of(java.util.Map.of(
                    "field", "date", "message", "Invalid Date Format for date", "rejectedValue", "2025-02-29",
                     "constraint", "FieldFormat", "rootBean", "FormattedRequest")), fault.getFaultContent().getFaultInfo());
        }
    }
    interface Create {}
    record Request(@NotBlank(groups = Create.class, message = "name required") String name) {}

    @Test
    void should_ReturnSameUtilityAndUseDefaults_When_RequestIsValid() {
        // given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        try (var utility = ValidationConfig.builder().withProcessor(processor).build()) {
            // when
            var result = utility.validateRequest("request");
            // then
            assertSame(utility, result);
            verify(processor).validateRequest("request", true);
            verifyNoMoreInteractions(processor);
        }
    }

    @Test
    void should_UseIndependentOptionsAndExplicitOverrides_When_BuilderIsReused() {
        // given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        Class<?>[] groups = {Create.class};
        var builder = ValidationConfig.builder().withProcessor(processor).useMessageDetails().withValidationGroups(groups);
        try (var first = builder.build();
             var second = builder.useMapDetails().withValidationGroups().build()) {
            groups[0] = Runnable.class;
            // when
            first.validateRequest("first");
            second.validateRequest("second");
            var chained = first.validateRequest("third", List.of(Runnable.class));
            first.validateRequest("fourth", true);
            first.validateRequest("fifth", true, List.of());
            // then
            assertNotSame(first, second);
            assertSame(first, chained);
            verify(processor).validateRequest("first", false, List.of(Create.class));
            verify(processor).validateRequest("second", true);
            verify(processor).validateRequest("third", false, List.of(Runnable.class));
            verify(processor).validateRequest("fourth", true, List.of(Create.class));
            verify(processor).validateRequest("fifth", true);
            verifyNoMoreInteractions(processor);
        }
    }

    @Test
    void should_PropagateProcessorFailure_When_CustomValidationFails() {
        // given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        var failure = new IllegalArgumentException("business validation failed");
        doThrow(failure).when(processor).validateRequest("request", true);
        try (var utility = ValidationConfig.builder().withProcessor(processor).build()) {
            // when
            var actual = assertThrows(IllegalArgumentException.class, () -> utility.validateRequest("request"));
            // then
            assertSame(failure, actual);
            verify(processor).validateRequest("request", true);
            verifyNoMoreInteractions(processor);
        }
    }

    @Test
    void should_RejectInvalidCallsBeforeDelegation_When_RequestOrGroupsAreInvalid() {
        // given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        List<Class<?>> nullGroup = new ArrayList<>();
        nullGroup.add(null);
        try (var utility = ValidationConfig.builder().withProcessor(processor).build()) {
            // when
            var request = assertThrows(NullPointerException.class, () -> utility.validateRequest(null));
            var groups = assertThrows(NullPointerException.class, () -> utility.validateRequest("r", (List<Class<?>>) null));
            var item = assertThrows(NullPointerException.class, () -> utility.validateRequest("r", nullGroup));
            var type = assertThrows(IllegalArgumentException.class, () -> utility.validateRequest("r", List.of(String.class)));
            // then
            assertEquals("request must not be null", request.getMessage());
            assertEquals("groups must not be null", groups.getMessage());
            assertNotNull(item);
            assertEquals("validation groups must be interfaces", type.getMessage());
            verifyNoInteractions(processor);
        }
    }

    @Test
    void should_ValidateRealConstraintsWithoutLogger_When_DefaultBehaviorIsSelected() {
        // given
        try (var utility = ValidationConfig.builder().useDefaultBehavior().withValidationGroups(Create.class).build()) {
            // when
            var valid = utility.validateRequest(new Request("valid"));
            var fault = assertThrows(ValidationFaultException.class, () -> utility.validateRequest(new Request("")));
            // then
            assertSame(utility, valid);
            assertEquals(HttpStatus.BAD_REQUEST, fault.getFaultContent().getStatusCode());
            assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), fault.getFaultContent().getErrorCode());
            assertEquals("Invalid Request", fault.getFaultContent().getErrorMessage());
            assertEquals(List.of(java.util.Map.of("field", "name", "message", "name required",
                    "rejectedValue", "", "constraint", "NotBlank", "rootBean", "Request")), fault.getFaultContent().getFaultInfo());
        }
    }

    @Test
    void should_UseCallerValidator_When_ExternalValidatorIsConfigured() {
        // given
        Validator validator = mock(Validator.class);
        when(validator.validate("r")).thenReturn(Set.of());
        var utility = ValidationConfig.builder().withValidator(validator).build();
        // when
        var result = utility.validateRequest("r");
        utility.close();
        utility.close();
        // then
        assertSame(utility, result);
        verify(validator).validate("r");
        verifyNoMoreInteractions(validator);
        var error = assertThrows(IllegalStateException.class, () -> utility.validateRequest("r"));
        assertEquals("ValidationUtility is closed", error.getMessage());
    }

    @Test
    void should_CloseOwnedFactoryOnlyOnce_When_UtilityIsClosed() {
        // given
        ValidatorFactory factory = mock(ValidatorFactory.class);
        Validator validator = mock(Validator.class);
        when(factory.getValidator()).thenReturn(validator);
        when(validator.validate("r")).thenReturn(Set.of());
        try (var bootstrap = mockStatic(Validation.class)) {
            bootstrap.when(Validation::buildDefaultValidatorFactory).thenReturn(factory);
            var utility = ValidationConfig.builder().useDefaultBehavior().build();
            // when
            utility.validateRequest("r");
            utility.close();
            utility.close();
            // then
            bootstrap.verify(Validation::buildDefaultValidatorFactory);
            bootstrap.verifyNoMoreInteractions();
            verify(factory).getValidator();
            verify(factory).close();
            verifyNoMoreInteractions(factory);
            verify(validator).validate("r");
            verifyNoMoreInteractions(validator);
        }
    }

    @Test
    void should_CloseFactory_When_ProcessorCreationFails() {
        // given
        ValidatorFactory factory = mock(ValidatorFactory.class);
        var failure = new IllegalStateException("validator unavailable");
        when(factory.getValidator()).thenThrow(failure);
        try (var bootstrap = mockStatic(Validation.class)) {
            bootstrap.when(Validation::buildDefaultValidatorFactory).thenReturn(factory);
            // when
            var error = assertThrows(IllegalStateException.class, () -> ValidationConfig.builder().build());
            // then
            assertSame(failure, error);
            bootstrap.verify(Validation::buildDefaultValidatorFactory);
            verify(factory).getValidator();
            verify(factory).close();
            verifyNoMoreInteractions(factory);
        }
    }

    @Test
    void should_ReturnMessageDetails_When_MessageFormatIsConfigured() {
        // given
        try (var utility = ValidationConfig.builder().useMessageDetails().withValidationGroups(Create.class).build()) {
            // when
            var fault = assertThrows(ValidationFaultException.class, () -> utility.validateRequest(new Request("")));
            // then
            assertEquals(Set.of("name required"), fault.getFaultContent().getFaultInfo());
            assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), fault.getFaultContent().getErrorCode());
            assertEquals("Invalid Request", fault.getFaultContent().getErrorMessage());
            assertEquals(HttpStatus.BAD_REQUEST, fault.getFaultContent().getStatusCode());
        }
    }

    @Test
    void should_ResetAllOptions_When_DefaultBehaviorIsSelectedLast() {
        // given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        Validator validator = mock(Validator.class);
        var builder = ValidationConfig.builder().withProcessor(processor).withValidator(validator)
                .useMessageDetails().withValidationGroups(Create.class).useDefaultBehavior();
        // when
        try (var utility = builder.build()) {
            var result = utility.validateRequest(new Request(""));
            // then
            assertSame(utility, result);
            verifyNoInteractions(processor, validator);
        }
    }

    @Test
    void should_ReplaceStrategy_When_ProcessorOrValidatorIsConfiguredLast() {
        // given
        BaseValidationProcessor processor = mock(BaseValidationProcessor.class);
        Validator validator = mock(Validator.class);
        when(validator.validate("validator")).thenReturn(Set.of());
        var builder = ValidationConfig.builder();
        try (var first = builder.withValidator(validator).withProcessor(processor).build();
             var second = builder.withValidator(validator).build()) {
            // when
            first.validateRequest("processor");
            second.validateRequest("validator");
            // then
            verify(processor).validateRequest("processor", true);
            verify(validator).validate("validator");
            verifyNoMoreInteractions(processor, validator);
        }
    }
}

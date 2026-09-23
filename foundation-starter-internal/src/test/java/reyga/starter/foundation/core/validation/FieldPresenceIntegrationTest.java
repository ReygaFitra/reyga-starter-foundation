package reyga.starter.foundation.core.validation;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.core.annotation.FieldPresence;
import reyga.starter.foundation.core.exception.ValidationFaultException;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class FieldPresenceIntegrationTest {
    interface Create {}
    record Input(@FieldPresence(message = "name required") String name,
                 @FieldPresence(nullable = true) List<String> tags,
                 @FieldPresence int count, @FieldPresence boolean active) {}
    record Grouped(@FieldPresence(groups = Create.class, message = "id required") String id) {}
    record Elements(@FieldPresence List<@FieldPresence(message = "element required") String> names) {}
    record NullableBlank(@FieldPresence(nullable = true, allowBlank = true) String value) {}
    record OptionalContainer(@FieldPresence(nullable = true,
            payload = jakarta.validation.valueextraction.Unwrapping.Skip.class)
            java.util.OptionalInt value) {}

    @Test
    void should_ValidateOptionalContainer_When_UnwrappingIsSkipped() {
        // given
        try (var utility = ValidationConfig.builder().useMessageDetails().build()) {
            // when
            var nullable = utility.validateRequest(new OptionalContainer(null));
            var present = utility.validateRequest(new OptionalContainer(java.util.OptionalInt.of(0)));
            var error = assertThrows(ValidationFaultException.class,
                    () -> utility.validateRequest(new OptionalContainer(java.util.OptionalInt.empty())));
            // then
            assertSame(utility, nullable);
            assertSame(utility, present);
            assertEquals(Set.of("must satisfy nullable=true and allowBlank=false"), error.getFaultContent().getFaultInfo());
            assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), error.getFaultContent().getErrorCode());
            assertEquals("Invalid Request", error.getFaultContent().getErrorMessage());
            assertEquals(HttpStatus.BAD_REQUEST, error.getFaultContent().getStatusCode());
        }
    }

    @Test
    void should_AcceptNullableAndPrimitiveFields_When_PresencePolicyIsSatisfied() {
        // given
        try (var utility = ValidationConfig.builder().build()) {
            // when
            var result = utility.validateRequest(new Input("name", null, 0, false));
            // then
            assertSame(utility, result);
        }
    }

    @Test
    void should_ReturnCustomMessage_When_RequiredTextIsBlank() {
        // given
        try (var utility = ValidationConfig.builder().useMessageDetails().build()) {
            // when
            var error = assertThrows(ValidationFaultException.class,
                    () -> utility.validateRequest(new Input(" \t", null, 0, false)));
            // then
            assertEquals(Set.of("name required"), error.getFaultContent().getFaultInfo());
            assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), error.getFaultContent().getErrorCode());
            assertEquals("Invalid Request", error.getFaultContent().getErrorMessage());
            assertEquals(HttpStatus.BAD_REQUEST, error.getFaultContent().getStatusCode());
        }
    }

    @Test
    void should_RejectEmptyCollection_When_NullIsAllowedButBlankIsNot() {
        // given
        try (var utility = ValidationConfig.builder().useMessageDetails().build()) {
            // when
            var error = assertThrows(ValidationFaultException.class,
                    () -> utility.validateRequest(new Input("name", List.of(), 0, false)));
            // then
            assertEquals(Set.of("must satisfy nullable=true and allowBlank=false"), error.getFaultContent().getFaultInfo());
            assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), error.getFaultContent().getErrorCode());
            assertEquals("Invalid Request", error.getFaultContent().getErrorMessage());
            assertEquals(HttpStatus.BAD_REQUEST, error.getFaultContent().getStatusCode());
        }
    }

    @Test
    void should_ValidateOnlySelectedGroup_When_GroupConstraintIsDeclared() {
        // given
        try (var utility = ValidationConfig.builder().useMessageDetails().build()) {
            // when
            var result = utility.validateRequest(new Grouped(null));
            var error = assertThrows(ValidationFaultException.class,
                    () -> utility.validateRequest(new Grouped(null), List.of(Create.class)));
            // then
            assertSame(utility, result);
            assertEquals(Set.of("id required"), error.getFaultContent().getFaultInfo());
            assertEquals(HttpStatus.BAD_REQUEST, error.getFaultContent().getStatusCode());
            assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), error.getFaultContent().getErrorCode());
            assertEquals("Invalid Request", error.getFaultContent().getErrorMessage());
        }
    }

    @Test
    void should_ValidateElements_When_TypeUseConstraintIsDeclared() {
        // given
        try (var utility = ValidationConfig.builder().useMessageDetails().build()) {
            // when
            var error = assertThrows(ValidationFaultException.class,
                    () -> utility.validateRequest(new Elements(List.of(""))));
            // then
            assertEquals(Set.of("element required"), error.getFaultContent().getFaultInfo());
            assertEquals(HttpStatus.BAD_REQUEST, error.getFaultContent().getStatusCode());
            assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), error.getFaultContent().getErrorCode());
            assertEquals("Invalid Request", error.getFaultContent().getErrorMessage());
        }
    }

    @Test
    void should_AcceptNullAndBlank_When_BothOptionsAreEnabled() {
        // given
        try (var utility = ValidationConfig.builder().build()) {
            // when
            var nullResult = utility.validateRequest(new NullableBlank(null));
            var blankResult = utility.validateRequest(new NullableBlank(""));
            // then
            assertSame(utility, nullResult);
            assertSame(utility, blankResult);
        }
    }
}

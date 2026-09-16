package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.model.dto.response.FieldErrorDetail;
import reyga.starter.foundation.core.exception.ValidationFaultException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ValidationProcessorTest {

    private ValidationProcessor processor;
    private CommonLogger logger;

    @BeforeEach
    void setUp() {
        processor = new ValidationProcessor();
        logger = mock(CommonLogger.class);
        processor.logger = logger;
    }

    @Test
    void should_ThrowValidationFaultWithUniqueMessagesAndLogDetails_When_SetViolationsArePresent() {
        // given
        ConstraintViolation<Object> firstViolation = mockViolation("name", "must not be null", null);
        ConstraintViolation<Object> secondViolation = mockViolation("alias", "must not be null", "");

        // when
        ValidationFaultException result = assertThrows(
                ValidationFaultException.class,
                () -> processor.violationsSetHandle(Set.of(firstViolation, secondViolation))
        );

        // then
        assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), result.getFaultContent().getErrorCode());
        assertEquals("Invalid Request", result.getFaultContent().getErrorMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getFaultContent().getStatusCode());
        assertEquals(Set.of("must not be null"), result.getFaultContent().getFaultInfo());
        assertEquals("Validation Exception", result.getBusiness());
        assertEquals("One or more request fields are invalid.", result.getAdditionalInfo());
        assertNotNull(result.getFieldErrorList());
        assertEquals(2, result.getFieldErrorList().size());
        assertFieldError(result.getFieldErrorList().get(0), "alias", "must not be null");
        assertFieldError(result.getFieldErrorList().get(1), "name", "must not be null");
        verify(logger).warn("Validation Error Count", 1);
        verify(logger).warn("Validation Error Detail", Set.of("must not be null"));
        verifyNoMoreInteractions(logger);
    }

    @Test
    void should_ThrowValidationFaultWithFieldDetailsAndLogDetails_When_MapViolationsArePresent() {
        // given
        String rejectedValue = "x".repeat(201);
        ConstraintViolation<Object> violation = mockViolation("name", "must not be null", rejectedValue);

        // when
        ValidationFaultException result = assertThrows(
                ValidationFaultException.class,
                () -> processor.violationsMapHandle(Set.of(violation))
        );

        // then
        assertEquals(ServiceCodeEnum.VALIDATION_ERROR.getCode(), result.getFaultContent().getErrorCode());
        assertEquals("Invalid Request", result.getFaultContent().getErrorMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getFaultContent().getStatusCode());
        List<?> details = (List<?>) result.getFaultContent().getFaultInfo();
        assertEquals(1, details.size());
        assertEquals(Map.of(
                "field", "name",
                "message", "must not be null",
                "rejectedValue", "x".repeat(200) + "...(truncated)",
                "constraint", "NotNull",
                "rootBean", "Object"
        ), details.getFirst());
        assertEquals("Validation Exception", result.getBusiness());
        assertEquals("One or more request fields are invalid.", result.getAdditionalInfo());
        assertNotNull(result.getFieldErrorList());
        assertEquals(1, result.getFieldErrorList().size());
        assertFieldError(result.getFieldErrorList().getFirst(), "name", "must not be null");
        verify(logger).warn("Validation Error Count", 1);
        verify(logger).warn("Validation Error Detail", details);
        verifyNoMoreInteractions(logger);
    }

    @Test
    void should_NotThrowOrLog_When_SetViolationsAreEmpty() {
        // given
        Set<ConstraintViolation<Object>> violations = Set.of();

        // when
        assertDoesNotThrow(() -> processor.violationsSetHandle(violations));

        // then
        verifyNoInteractions(logger);
    }

    @Test
    void should_NotThrowOrLog_When_MapViolationsAreEmpty() {
        // given
        Set<ConstraintViolation<Object>> violations = Set.of();

        // when
        assertDoesNotThrow(() -> processor.violationsMapHandle(violations));

        // then
        verifyNoInteractions(logger);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private ConstraintViolation<Object> mockViolation(String field, String message, Object invalidValue) {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        ConstraintDescriptor descriptor = mock(ConstraintDescriptor.class);
        NotNull annotation = mock(NotNull.class);
        when(path.toString()).thenReturn(field);
        when(annotation.annotationType()).thenReturn((Class) NotNull.class);
        when(descriptor.getAnnotation()).thenReturn(annotation);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        when(violation.getInvalidValue()).thenReturn(invalidValue);
        when(violation.getConstraintDescriptor()).thenReturn(descriptor);
        when(violation.getRootBeanClass()).thenReturn(Object.class);
        return violation;
    }

    private void assertFieldError(FieldErrorDetail result, String field, String message) {
        assertEquals(field, result.getField());
        assertEquals(message, result.getMessage());
        assertNotNull(result.getTimestamp());
    }
}

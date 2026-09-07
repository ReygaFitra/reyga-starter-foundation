package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void should_ThrowAppFaultWithUniqueMessagesAndLogDetails_When_SetViolationsArePresent() {
        // given
        ConstraintViolation<Object> firstViolation = mockViolation("name", "must not be null", null);
        ConstraintViolation<Object> secondViolation = mockViolation("alias", "must not be null", "");

        // when
        AppFaultException result = assertThrows(
                AppFaultException.class,
                () -> processor.violationsSetHandle(Set.of(firstViolation, secondViolation))
        );

        // then
        assertEquals("01", result.getErrorCode());
        assertEquals("Invalid Request", result.getErrorMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(Set.of("must not be null"), result.getFaultInfo());
        verify(logger).warn("Validation Error Count", 1);
        verify(logger).warn("Validation Error Detail", Set.of("must not be null"));
        verifyNoMoreInteractions(logger);
    }

    @Test
    void should_ThrowAppFaultWithFieldDetailsAndLogDetails_When_MapViolationsArePresent() {
        // given
        String rejectedValue = "x".repeat(201);
        ConstraintViolation<Object> violation = mockViolation("name", "must not be null", rejectedValue);

        // when
        AppFaultException result = assertThrows(
                AppFaultException.class,
                () -> processor.violationsMapHandle(Set.of(violation))
        );

        // then
        assertEquals("01", result.getErrorCode());
        assertEquals("Invalid Request", result.getErrorMessage());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        List<?> details = (List<?>) result.getFaultInfo();
        assertEquals(1, details.size());
        assertEquals(Map.of(
                "field", "name",
                "message", "must not be null",
                "rejectedValue", "x".repeat(200) + "...(truncated)",
                "constraint", "NotNull",
                "rootBean", "Object"
        ), details.getFirst());
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
}

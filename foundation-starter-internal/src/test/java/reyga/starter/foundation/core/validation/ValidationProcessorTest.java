package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ValidationProcessorTest {

    @Test
    void violationsSetHandle_throwsAppFaultException_whenViolationsPresent() {
        ValidationProcessor processor = new ValidationProcessor();
        ConstraintViolation<Object> violation = mockViolation("msg");

        assertThrows(AppFaultException.class, () -> processor.violationsSetHandle(Set.of(violation)));
    }

    @Test
    void violationsMapHandle_throwsAppFaultException_whenViolationsPresent() {
        ValidationProcessor processor = new ValidationProcessor();
        ConstraintViolation<Object> violation = mockViolation("msg");

        assertThrows(AppFaultException.class, () -> processor.violationsMapHandle(Set.of(violation)));
    }

    @Test
    void violationsSetHandle_noException_whenEmpty() {
        ValidationProcessor processor = new ValidationProcessor();
        assertDoesNotThrow(() -> processor.violationsSetHandle(Set.of()));
    }

    private ConstraintViolation<Object> mockViolation(String message) {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("field");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        when(violation.getInvalidValue()).thenReturn("value");
        when(violation.getRootBeanClass()).thenReturn(Object.class);
        return violation;
    }
}

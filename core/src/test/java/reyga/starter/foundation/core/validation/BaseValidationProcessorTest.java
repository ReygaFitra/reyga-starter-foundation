package reyga.starter.foundation.core.validation;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reyga.starter.foundation.core.annotation.FieldLength;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BaseValidationProcessorTest {

    private TrackingValidationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TrackingValidationProcessor();
    }

    @Test
    void should_InvokeSetHandlerWithNoViolations_When_DefaultValidationSucceeds() {
        // Given
        TestRequest request = new TestRequest("valid");

        // When
        processor.validateRequest(request, false);

        // Then
        assertEquals("set", processor.selectedHandler);
        assertNotNull(processor.violations);
        assertTrue(processor.violations.isEmpty());
    }

    @Test
    void should_InvokeMapHandlerWithViolation_When_DefaultValidationFails() {
        // Given
        TestRequest request = new TestRequest("x");

        // When
        processor.validateRequest(request, true);

        // Then
        assertEquals("map", processor.selectedHandler);
        assertEquals(1, processor.violations.size());
        ConstraintViolation<?> violation = processor.violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("NAME field minimum length 3 characters", violation.getMessage());
        assertSame(request, violation.getRootBean());
    }

    @Test
    void should_IgnoreGroupedConstraint_When_ValidationGroupIsNotProvided() {
        // Given
        GroupedRequest request = new GroupedRequest("x");

        // When
        processor.validateRequest(request, false);

        // Then
        assertEquals("set", processor.selectedHandler);
        assertTrue(processor.violations.isEmpty());
    }

    @Test
    void should_InvokeMapHandlerWithViolation_When_ValidationGroupFails() {
        // Given
        GroupedRequest request = new GroupedRequest("x");
        List<Class<StrictValidation>> groups = List.of(StrictValidation.class);

        // When
        processor.validateRequest(request, true, groups);

        // Then
        assertEquals("map", processor.selectedHandler);
        assertEquals(1, processor.violations.size());
        ConstraintViolation<?> violation = processor.violations.iterator().next();
        assertEquals("code", violation.getPropertyPath().toString());
        assertEquals("CODE field minimum length 3 characters", violation.getMessage());
        assertSame(request, violation.getRootBean());
    }

    private interface StrictValidation {
    }

    private static final class TestRequest {
        @FieldLength(fieldName = "name", min = 3, max = 10)
        private final String name;

        private TestRequest(String name) {
            this.name = name;
        }
    }

    private static final class GroupedRequest {
        @FieldLength(fieldName = "code", min = 3, max = 10, groups = StrictValidation.class)
        private final String code;

        private GroupedRequest(String code) {
            this.code = code;
        }
    }

    private static final class TrackingValidationProcessor extends BaseValidationProcessor {
        private String selectedHandler;
        private Set<? extends ConstraintViolation<?>> violations;

        @Override
        protected <T> void violationsSetHandle(Set<ConstraintViolation<T>> violations) {
            selectedHandler = "set";
            this.violations = violations;
        }

        @Override
        protected <T> void violationsMapHandle(Set<ConstraintViolation<T>> violations) {
            selectedHandler = "map";
            this.violations = violations;
        }
    }
}

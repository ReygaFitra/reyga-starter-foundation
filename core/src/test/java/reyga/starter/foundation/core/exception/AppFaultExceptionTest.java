package reyga.starter.foundation.core.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class AppFaultExceptionTest {

    @Test
    void should_ReturnCompleteContent_When_AllFaultValuesAreProvided() {
        // Given
        Object faultInfo = "fault";

        // When
        AppFaultContent result = AppFaultContent.buildAppFaultContent(
                "message", "01", "error", faultInfo, HttpStatus.BAD_REQUEST
        );

        // Then
        assertEquals("message", result.getMessage());
        assertEquals("01", result.getErrorCode());
        assertEquals("error", result.getErrorMessage());
        assertSame(faultInfo, result.getFaultInfo());
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void should_ReturnExceptionValues_When_DirectValuesAreProvided() {
        // Given
        Object faultInfo = new Object();

        // When
        AppFaultException result = new AppFaultException(
                "02", "not found", faultInfo, HttpStatus.NOT_FOUND
        );

        // Then
        assertException(result, "02", "not found", faultInfo, HttpStatus.NOT_FOUND, null);
    }

    @Test
    void should_ReturnExceptionValuesAndCause_When_DirectValuesAndCauseAreProvided() {
        // Given
        RuntimeException cause = new RuntimeException("root cause");
        Object faultInfo = new Object();

        // When
        AppFaultException result = new AppFaultException(
                "03", "conflict", faultInfo, cause, HttpStatus.CONFLICT
        );

        // Then
        assertException(result, "03", "conflict", faultInfo, HttpStatus.CONFLICT, cause);
    }

    @Test
    void should_ReturnExceptionValues_When_ContentIsProvided() {
        // Given
        Object faultInfo = new Object();
        AppFaultContent content = AppFaultContent.buildAppFaultContent(
                "content message", "04", "unprocessable", faultInfo, HttpStatus.UNPROCESSABLE_ENTITY
        );

        // When
        AppFaultException result = new AppFaultException(content);

        // Then
        assertException(result, "04", "unprocessable", faultInfo, HttpStatus.UNPROCESSABLE_ENTITY, null);
    }

    @Test
    void should_ReturnExceptionValuesAndCause_When_ContentAndCauseAreProvided() {
        // Given
        IllegalStateException cause = new IllegalStateException("root cause");
        Object faultInfo = new Object();
        AppFaultContent content = AppFaultContent.buildAppFaultContent(
                "content message", "05", "failed", faultInfo, HttpStatus.INTERNAL_SERVER_ERROR
        );

        // When
        AppFaultException result = new AppFaultException(content, cause);

        // Then
        assertException(result, "05", "failed", faultInfo, HttpStatus.INTERNAL_SERVER_ERROR, cause);
        assertTrue(result.toString().contains("errorCode='05'"));
        assertTrue(result.toString().contains("errorMessage='failed'"));
        assertTrue(result.toString().contains("statusCode=500 INTERNAL_SERVER_ERROR"));
    }

    private static void assertException(AppFaultException exception, String code, String message,
                                        Object faultInfo, HttpStatus status, Throwable cause) {
        assertEquals(message, exception.getMessage());
        assertEquals(message, exception.getErrorMessage());
        assertEquals(code, exception.getErrorCode());
        assertSame(faultInfo, exception.getFaultInfo());
        assertEquals(status, exception.getStatusCode());
        assertSame(cause, exception.getCause());
    }
}

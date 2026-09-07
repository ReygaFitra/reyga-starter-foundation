package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.HeaderEnum;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseExceptionHandlerTest {

    @Test
    void should_ReturnGlobalErrorResponse_When_GlobalExceptionIsHandled() {
        // Given
        TestHandler handler = spy(new TestHandler());
        HttpServletRequest request = mock(HttpServletRequest.class);
        Exception failure = new RuntimeException("global failure");

        // When
        ResponseEntity<String> result = handler.handleGlobalErrorException(failure, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("GLOBAL", result.getBody());
        verify(request).setAttribute(HeaderEnum.EXCEPTION.getValue(), failure);
        verify(handler).processGlobalErrorHandler(failure, request);
        verifyNoMoreInteractions(request);
    }

    @Test
    void should_ReturnDatabaseErrorResponse_When_DatabaseExceptionIsHandled() {
        // Given
        TestHandler handler = spy(new TestHandler());
        HttpServletRequest request = mock(HttpServletRequest.class);
        Exception failure = new RuntimeException("database failure");

        // When
        ResponseEntity<String> result = handler.handleDatabaseErrorException(failure, request);

        // Then
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, result.getStatusCode());
        assertEquals("DATABASE", result.getBody());
        verify(request).setAttribute(HeaderEnum.EXCEPTION.getValue(), failure);
        verify(handler).processDatabaseErrorHandler(failure, request);
        verifyNoMoreInteractions(request);
    }

    private static class TestHandler extends BaseExceptionHandler<String> {
        @Override
        protected ResponseEntity<String> processGlobalErrorHandler(Exception exception, HttpServletRequest request) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("GLOBAL");
        }

        @Override
        protected ResponseEntity<String> processDatabaseErrorHandler(Exception exception, HttpServletRequest request) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("DATABASE");
        }
    }
}

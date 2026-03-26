package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.jpa.JpaSystemException;
import reyga.starter.foundation.common.model.dto.response.ResponseError;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultExceptionHandlerTest {

    @Test
    void processGlobalErrorHandler_handlesIllegalArgument() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();
        HttpServletRequest request = mockRequest();

        ResponseEntity<ResponseError> response = handler.handleGlobalErrorException(
                new IllegalArgumentException("bad"), request
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("91", response.getBody().getCode());
        assertEquals("GENERAL ERROR", response.getBody().getMessage());
    }

    @Test
    void processGlobalErrorHandler_handlesGenericException() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();
        HttpServletRequest request = mockRequest();

        ResponseEntity<ResponseError> response = handler.handleGlobalErrorException(
                new RuntimeException("fail"), request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("99", response.getBody().getCode());
        assertEquals("INTERNAL SERVER ERROR", response.getBody().getMessage());
    }

    @Test
    void processDatabaseErrorHandler_handlesJpaSystemException() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();
        HttpServletRequest request = mockRequest();

        ResponseEntity<ResponseError> response = handler.handleDatabaseErrorException(
                new JpaSystemException(new RuntimeException("db")), request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("99", response.getBody().getCode());
        assertEquals("DATABASE ERROR", response.getBody().getMessage());
    }

    @Test
    void processDatabaseErrorHandler_handlesDataAccessException() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();
        HttpServletRequest request = mockRequest();

        ResponseEntity<ResponseError> response = handler.handleDatabaseErrorException(
                new DataAccessException("db") {}, request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("99", response.getBody().getCode());
        assertEquals("DATABASE ERROR", response.getBody().getMessage());
    }

    @Test
    void processAppFaultErrorHandler_returnsAppFaultResponse() {
        DefaultExceptionHandler handler = new DefaultExceptionHandler();
        HttpServletRequest request = mockRequest();
        AppFaultException ex = new AppFaultException(
                AppFaultContent.buildAppFaultContent("msg", "01", "err", "fault", HttpStatus.CONFLICT)
        );

        ResponseEntity<ResponseError> response = handler.handleAppFaultException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("01", response.getBody().getCode());
        assertEquals("err", response.getBody().getMessage());
    }

    private HttpServletRequest mockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, Object> attributes = new HashMap<>();
        doAnswer(invocation -> {
            attributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(request).setAttribute(anyString(), any());
        when(request.getAttribute(anyString())).thenAnswer(invocation -> attributes.get(invocation.getArgument(0)));
        return request;
    }
}

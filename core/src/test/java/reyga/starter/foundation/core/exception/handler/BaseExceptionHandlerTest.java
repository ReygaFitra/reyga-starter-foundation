package reyga.starter.foundation.core.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.core.exception.AppFaultContent;
import reyga.starter.foundation.core.exception.AppFaultException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BaseExceptionHandlerTest {

    @Test
    void handleGlobalErrorException_setsAttributeAndDelegates() {
        TestHandler handler = new TestHandler();
        HttpServletRequest request = mockRequest();
        Exception ex = new RuntimeException("fail");

        ResponseEntity<String> response = handler.handleGlobalErrorException(ex, request);

        assertEquals("GLOBAL", response.getBody());
        assertSame(ex, request.getAttribute(HeaderEnum.EXCEPTION.getValue()));
        assertEquals("global", handler.lastHandler);
    }

    @Test
    void handleDatabaseErrorException_setsAttributeAndDelegates() {
        TestHandler handler = new TestHandler();
        HttpServletRequest request = mockRequest();
        Exception ex = new RuntimeException("db");

        ResponseEntity<String> response = handler.handleDatabaseErrorException(ex, request);

        assertEquals("DB", response.getBody());
        assertSame(ex, request.getAttribute(HeaderEnum.EXCEPTION.getValue()));
        assertEquals("db", handler.lastHandler);
    }

    @Test
    void handleAppFaultException_setsAttributeAndDelegates() {
        TestHandler handler = new TestHandler();
        HttpServletRequest request = mockRequest();
        AppFaultException ex = new AppFaultException(
                AppFaultContent.buildAppFaultContent("msg", "01", "err", "fault", null)
        );

        ResponseEntity<String> response = handler.handleAppFaultException(ex, request);

        assertEquals("APP", response.getBody());
        assertSame(ex, request.getAttribute(HeaderEnum.EXCEPTION.getValue()));
        assertEquals("app", handler.lastHandler);
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

    private static class TestHandler extends BaseExceptionHandler<String> {
        private String lastHandler;

        @Override
        protected ResponseEntity<String> processGlobalErrorHandler(Exception exception, HttpServletRequest servletRequest) {
            lastHandler = "global";
            return ResponseEntity.ok("GLOBAL");
        }

        @Override
        protected ResponseEntity<String> processDatabaseErrorHandler(Exception exception, HttpServletRequest servletRequest) {
            lastHandler = "db";
            return ResponseEntity.ok("DB");
        }

        @Override
        protected ResponseEntity<String> processAppFaultErrorHandler(AppFaultException appFaultException, HttpServletRequest servletRequest) {
            lastHandler = "app";
            return ResponseEntity.ok("APP");
        }
    }
}

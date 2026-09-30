package reyga.starter.foundation.logging.service;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import reyga.starter.foundation.common.enumeration.StarterHeaderEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.ServiceExecutionSummary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefaultLoggingServiceTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void should_InitializeRequestContextWithoutLogging_When_PreHandleIsInvoked() {
        DefaultLoggingService service = new DefaultLoggingService();
        CommonLogger logger = mock(CommonLogger.class);
        service.logger = logger;
        HttpServletRequest request = mockRequest(Map.of("x-test", "1"));
        HttpServletResponse response = mock(HttpServletResponse.class);

        boolean result = service.preHandle(request, response, new Object());

        assertTrue(result);
        assertTrue(request.getAttribute("x-starter-duration-time") instanceof Long);
        assertTrue(request.getAttribute(StarterHeaderEnum.REQUEST_ID.getValue()) instanceof String);
        assertEquals("GET", request.getAttribute(StarterHeaderEnum.METHOD.getValue()));
        assertEquals("/test", request.getAttribute(StarterHeaderEnum.REQUEST_ENDPOINT.getValue()));
        assertEquals("1", MDC.get("x-test"));
        verifyNoInteractions(logger, response);
    }

    @Test
    void should_ContinueWithoutContextMutation_When_DispatcherIsNotRequest() {
        DefaultLoggingService service = new DefaultLoggingService();
        HttpServletRequest request = mockRequest(Map.of());
        when(request.getDispatcherType()).thenReturn(DispatcherType.FORWARD);
        HttpServletResponse response = mock(HttpServletResponse.class);

        boolean result = service.preHandle(request, response, new Object());

        assertTrue(result);
        assertNull(request.getAttribute("x-starter-duration-time"));
        verifyNoInteractions(response);
    }

    @Test
    void should_LogSummaryAndClearMdc_When_RequestCompletes() throws Exception {
        DefaultLoggingService service = new DefaultLoggingService();
        CommonLogger logger = mock(CommonLogger.class);
        service.logger = logger;
        HttpServletRequest request = mockRequest(Map.of(
                StarterHeaderEnum.REQUEST_ID.getValue(), "request-01",
                StarterHeaderEnum.USERNAME.getValue(), "operator",
                StarterHeaderEnum.USER_AGENT.getValue(), "test-agent"
        ));
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(204);
        HandlerMethod handler = handlerMethod();
        service.preHandle(request, response, handler);

        service.afterCompletion(request, response, handler, null);

        ArgumentCaptor<ServiceExecutionSummary> summaryCaptor =
                ArgumentCaptor.forClass(ServiceExecutionSummary.class);
        verify(logger).infoServiceSummary(summaryCaptor.capture());
        ServiceExecutionSummary summary = summaryCaptor.getValue();
        assertTrue(summary.handler().endsWith("DefaultLoggingServiceTest$Dummy#handle"));
        assertEquals("request-01", summary.requestId());
        assertEquals("operator", summary.username());
        assertEquals("GET", summary.httpMethod());
        assertEquals(204, summary.statusCode());
        assertEquals("/test", summary.endpoint());
        assertEquals("127.0.0.1", summary.clientAddress());
        assertEquals("test-agent", summary.userAgent());
        assertTrue(summary.duration().endsWith(" ms"));
        assertTrue(summary.request().contains("requestBody=null"));
        assertNull(summary.response());
        assertNull(summary.error());
        assertNull(MDC.get(StarterHeaderEnum.SUMMARY_LOG.getValue()));
    }

    @Test
    void should_IncludeUnhandledExceptionAndClearMdc_When_RequestFails() throws Exception {
        DefaultLoggingService service = new DefaultLoggingService();
        CommonLogger logger = mock(CommonLogger.class);
        service.logger = logger;
        HttpServletRequest request = mockRequest(Map.of());
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(500);
        HandlerMethod handler = handlerMethod();
        IllegalStateException failure = new IllegalStateException("processing failed");
        service.preHandle(request, response, handler);

        service.afterCompletion(request, response, handler, failure);

        ArgumentCaptor<ServiceExecutionSummary> summaryCaptor =
                ArgumentCaptor.forClass(ServiceExecutionSummary.class);
        verify(logger).infoServiceSummary(summaryCaptor.capture());
        assertEquals(failure.toString(), summaryCaptor.getValue().error());
        assertNull(MDC.get(StarterHeaderEnum.SUMMARY_LOG.getValue()));
    }

    private HandlerMethod handlerMethod() throws NoSuchMethodException {
        return new HandlerMethod(
                new Dummy(),
                Dummy.class.getDeclaredMethod("handle", String.class)
        );
    }

    private HttpServletRequest mockRequest(Map<String, String> headers) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, Object> attributes = new HashMap<>();

        when(request.getHeaderNames()).thenReturn(new Enumeration<>() {
            private final java.util.Iterator<String> iterator = headers.keySet().iterator();

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public String nextElement() {
                return iterator.next();
            }
        });
        when(request.getParameterNames()).thenReturn(java.util.Collections.emptyEnumeration());
        when(request.getHeader(anyString())).thenAnswer(
                invocation -> headers.get(invocation.getArgument(0))
        );
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);

        doAnswer(invocation -> {
            attributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(request).setAttribute(anyString(), any());
        when(request.getAttribute(anyString())).thenAnswer(
                invocation -> attributes.get(invocation.getArgument(0))
        );

        return request;
    }

    private static class Dummy {
        void handle(String body) {
        }
    }
}

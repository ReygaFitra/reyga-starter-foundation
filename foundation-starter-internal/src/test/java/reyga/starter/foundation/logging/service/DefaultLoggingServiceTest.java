package reyga.starter.foundation.logging.service;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import reyga.starter.foundation.common.enumeration.HeaderEnum;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultLoggingServiceTest {

    @Test
    void requestBodyAdviceAdapter_setsAttributesAndMdc() throws Exception {
        DefaultLoggingService service = new DefaultLoggingService();
        HttpServletRequest request = mockRequest(Map.of("x-test", "1"));
        MethodParameter parameter = new MethodParameter(Dummy.class.getDeclaredMethod("handle", String.class), 0);

        service.requestBodyAdviceAdapter(
                request, "body", null, parameter, String.class, DummyConverter.class
        );

        assertNotNull(request.getAttribute(HeaderEnum.REQUEST_ID.getValue()));
        assertEquals("GET", request.getAttribute(HeaderEnum.METHOD.getValue()));
        assertEquals("/test", request.getAttribute(HeaderEnum.REQUEST_ENDPOINT.getValue()));
        assertEquals("1", MDC.get("x-test"));
        MDC.clear();
    }

    @Test
    void responseBodyAdviceAdapter_ignoresNonRequestDispatcher() throws Exception {
        DefaultLoggingService service = new DefaultLoggingService();
        HttpServletRequest request = mockRequest(Map.of());
        when(request.getDispatcherType()).thenReturn(DispatcherType.FORWARD);
        MethodParameter parameter = new MethodParameter(Dummy.class.getDeclaredMethod("handle", String.class), 0);

        service.responseBodyAdviceAdapter(request, mock(HttpServletResponse.class), parameter, "body", MediaType.APPLICATION_JSON, DummyConverter.class);
    }

    @Test
    void responseBodyAdviceAdapter_setsSummaryAndClearsMdc() throws Exception {
        DefaultLoggingService service = new DefaultLoggingService();
        HttpServletRequest request = mockRequest(Map.of());
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        MethodParameter parameter = new MethodParameter(Dummy.class.getDeclaredMethod("handle", String.class), 0);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);

        request.setAttribute("x-starter-duration-time", System.currentTimeMillis());
        request.setAttribute(HeaderEnum.REQUEST.getValue(), "req");
        request.setAttribute(HeaderEnum.RESPONSE.getValue(), "resp");

        service.responseBodyAdviceAdapter(request, response, parameter, "body", MediaType.APPLICATION_JSON, DummyConverter.class);

        assertNull(MDC.get(HeaderEnum.SUMMARY_LOG.getValue()));
    }

    private HttpServletRequest mockRequest(Map<String, String> headers) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, Object> attributes = new HashMap<>();

        when(request.getHeaderNames()).thenReturn(new Enumeration<>() {
            private final java.util.Iterator<String> it = headers.keySet().iterator();
            @Override public boolean hasMoreElements() { return it.hasNext(); }
            @Override public String nextElement() { return it.next(); }
        });
        when(request.getHeader(anyString())).thenAnswer(invocation -> headers.get(invocation.getArgument(0)));
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);

        doAnswer(invocation -> {
            attributes.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(request).setAttribute(anyString(), any());
        when(request.getAttribute(anyString())).thenAnswer(invocation -> attributes.get(invocation.getArgument(0)));

        return request;
    }

    private static class Dummy {
        public void handle(String body) {
        }
    }

    private static class DummyConverter implements HttpMessageConverter<Object> {
        @Override public boolean canRead(Class<?> clazz, MediaType mediaType) { return false; }
        @Override public boolean canWrite(Class<?> clazz, MediaType mediaType) { return false; }
        @Override public java.util.List<MediaType> getSupportedMediaTypes() { return java.util.List.of(); }
        @Override public Object read(Class<?> clazz, org.springframework.http.HttpInputMessage inputMessage) { return null; }
        @Override public void write(Object o, MediaType contentType, org.springframework.http.HttpOutputMessage outputMessage) { }
    }
}

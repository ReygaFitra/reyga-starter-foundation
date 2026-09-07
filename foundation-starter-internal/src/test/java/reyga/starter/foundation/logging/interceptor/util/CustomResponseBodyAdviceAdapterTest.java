package reyga.starter.foundation.logging.interceptor.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.logging.service.LoggingService;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CustomResponseBodyAdviceAdapterTest {

    private LoggingService loggingService;
    private CustomResponseBodyAdviceAdapter adapter;
    private MethodParameter parameter;
    private Class<? extends HttpMessageConverter<?>> converterType;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        loggingService = mock(LoggingService.class);
        adapter = new CustomResponseBodyAdviceAdapter(loggingService);
        Method method = TestController.class.getDeclaredMethod("handle");
        parameter = new MethodParameter(method, -1);
        converterType = converterType();
    }

    @Test
    void should_ReturnTrue_When_ConverterChecksSupport() {
        // given

        // when
        boolean result = adapter.supports(parameter, converterType);

        // then
        assertTrue(result);
        verifyNoInteractions(loggingService);
    }

    @Test
    void should_ReturnSameBodyAndDelegateLogging_When_ServletRequestAndResponseAreProvided() {
        // given
        String body = "response-body";
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        ServletServerHttpRequest request = new ServletServerHttpRequest(servletRequest);
        ServletServerHttpResponse response = new ServletServerHttpResponse(servletResponse);

        // when
        Object result = adapter.beforeBodyWrite(
                body, parameter, MediaType.APPLICATION_JSON, converterType, request, response
        );

        // then
        assertSame(body, result);
        verify(servletRequest).setAttribute(HeaderEnum.RESPONSE.getValue(), "\"response-body\"");
        verify(loggingService).responseBodyAdviceAdapter(
                servletRequest,
                servletResponse,
                parameter,
                body,
                MediaType.APPLICATION_JSON,
                converterType
        );
    }

    @Test
    void should_ThrowClassCastExceptionWithoutDependencyCall_When_RequestIsNotServletRequest() {
        // given
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        ServletServerHttpResponse response =
                new ServletServerHttpResponse(mock(HttpServletResponse.class));

        // when
        ClassCastException result = assertThrows(ClassCastException.class, () ->
                adapter.beforeBodyWrite(
                        "body", parameter, MediaType.APPLICATION_JSON,
                        converterType, request, response
                )
        );

        // then
        assertTrue(result.getMessage().contains("ServletServerHttpRequest"));
        verifyNoInteractions(loggingService, request);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Class<? extends HttpMessageConverter<?>> converterType() {
        return (Class) HttpMessageConverter.class;
    }

    private static class TestController {
        String handle() {
            return "body";
        }
    }
}

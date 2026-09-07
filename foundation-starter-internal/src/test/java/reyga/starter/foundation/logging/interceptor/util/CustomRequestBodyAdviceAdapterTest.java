package reyga.starter.foundation.logging.interceptor.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.logging.service.LoggingService;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CustomRequestBodyAdviceAdapterTest {

    private LoggingService loggingService;
    private CustomRequestBodyAdviceAdapter adapter;
    private MethodParameter parameter;
    private HttpInputMessage inputMessage;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        loggingService = mock(LoggingService.class);
        adapter = new CustomRequestBodyAdviceAdapter(loggingService);
        Method method = TestController.class.getDeclaredMethod("handle", String.class);
        parameter = new MethodParameter(method, 0);
        inputMessage = mock(HttpInputMessage.class);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void should_ReturnTrue_When_ConverterChecksSupport() {
        // given
        Class<? extends HttpMessageConverter<?>> converterType = converterType();

        // when
        boolean result = adapter.supports(parameter, String.class, converterType);

        // then
        assertTrue(result);
        verifyNoInteractions(loggingService, inputMessage);
    }

    @Test
    void should_ReturnSameBodyAndDelegateLogging_When_RequestContextIsAvailable() {
        // given
        String body = "request-body";
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));
        Class<? extends HttpMessageConverter<?>> converterType = converterType();

        // when
        Object result = adapter.afterBodyRead(
                body, inputMessage, parameter, String.class, converterType
        );

        // then
        assertSame(body, result);
        verify(loggingService).requestBodyAdviceAdapter(
                servletRequest, body, inputMessage, parameter, String.class, converterType
        );
        verify(servletRequest).setAttribute(HeaderEnum.REQUEST.getValue(), body);
        verifyNoInteractions(inputMessage);
    }

    @Test
    void should_ReturnSameBodyWithoutDependencyCall_When_RequestContextIsUnavailable() {
        // given
        String body = "request-body";
        Class<? extends HttpMessageConverter<?>> converterType = converterType();

        // when
        Object result = adapter.afterBodyRead(
                body, inputMessage, parameter, String.class, converterType
        );

        // then
        assertSame(body, result);
        verifyNoInteractions(loggingService, inputMessage);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Class<? extends HttpMessageConverter<?>> converterType() {
        return (Class) HttpMessageConverter.class;
    }

    private static class TestController {
        void handle(String body) {
        }
    }
}

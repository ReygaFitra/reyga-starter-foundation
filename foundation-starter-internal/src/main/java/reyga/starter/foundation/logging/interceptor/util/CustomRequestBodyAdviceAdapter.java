package reyga.starter.foundation.logging.interceptor.util;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.logging.service.LoggingService;

import java.lang.reflect.Type;

@ControllerAdvice
public class CustomRequestBodyAdviceAdapter extends RequestBodyAdviceAdapter {

    private final LoggingService loggingService;

    public CustomRequestBodyAdviceAdapter(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Override
    public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public @NonNull Object afterBodyRead(
            @NonNull Object body, @NonNull HttpInputMessage inputMessage, @NonNull MethodParameter parameter,
            @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest httpServletRequest = requestAttributes.getRequest();
            loggingService.requestBodyAdviceAdapter(httpServletRequest, body, inputMessage, parameter, targetType, converterType);
            httpServletRequest.setAttribute(HeaderEnum.REQUEST.getValue(), body);
        }
        return body;
    }
}

package reyga.starter.foundation.logging.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import java.lang.reflect.Type;

public interface LoggingService {

    void requestBodyAdviceAdapter(@NonNull HttpServletRequest request, @NonNull Object body, @NonNull HttpInputMessage inputMessage,
                                  @NonNull MethodParameter parameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType);

    void responseBodyAdviceAdapter(@NonNull HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable MethodParameter methodParameter, @Nullable Object body, @NonNull MediaType selectedContentType,
                                   @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType);
}

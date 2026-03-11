package reyga.starter.foundation.logging.service;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.logging.HttpHeaderBuilder;
import reyga.starter.foundation.common.model.dto.request.RequestLogging;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

public class DefaultLoggingService extends BaseLogging implements LoggingService {

    @Override
    public void requestBodyAdviceAdapter(@NonNull HttpServletRequest request, @NonNull Object body, @NonNull HttpInputMessage inputMessage,
                                         @NonNull MethodParameter parameter, @NonNull Type targetType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        Map<String, String> headersMap = HttpHeaderBuilder.buildHeadersMap(request);
        request.setAttribute("x-starter-duration-time", System.currentTimeMillis());

        headersMap.computeIfAbsent(HeaderEnum.REQUEST_ID.getValue(), k -> UUID.randomUUID().toString());
        headersMap.put(HeaderEnum.METHOD.getValue(), request.getMethod());
        headersMap.put(HeaderEnum.REQUEST_ENDPOINT.getValue(), request.getRequestURI());
        headersMap.put(HeaderEnum.FORWARDED_FOR.getValue(), request.getRemoteAddr());
        headersMap.put(HeaderEnum.ACCESS_TOKEN.getValue(), request.getHeader(HeaderEnum.ACCESS_TOKEN.getValue()));
        headersMap.put(HeaderEnum.USERNAME.getValue(), request.getHeader(HeaderEnum.USERNAME.getValue()));
        headersMap.put(HeaderEnum.USER_AGENT.getValue(), request.getHeader(HeaderEnum.USER_AGENT.getValue()));

        headersMap.forEach((key, value) -> {
            if (value != null) {
                MDC.put(key, value);
                request.setAttribute(key, value);
            }
        });

        if (parameter.getMethod() != null) {
            String methodName = parameter.getMethod().getName();
            log.infoServiceStart(methodName);
        }

        log.info(headersMap.toString());
    }

    @Override
    public void responseBodyAdviceAdapter(@NonNull HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable MethodParameter methodParameter, @Nullable Object body, @NonNull MediaType selectedContentType,
                                          @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType) {
        if (!DispatcherType.REQUEST.name().equals(request.getDispatcherType().name())) {
            return;
        }
        if (methodParameter == null || methodParameter.getMethod() == null) {
            return;
        }

        Long startTime = (Long) request.getAttribute("x-starter-duration-time");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put(HeaderEnum.RESPONSE_TIME.getValue(), duration + " ms");
            request.setAttribute(HeaderEnum.RESPONSE_TIME.getValue(), duration + " ms");
        }

        RequestLogging requestDto = new RequestLogging();
        Object reqBodyAttr = request.getAttribute(HeaderEnum.REQUEST.getValue());
        if (reqBodyAttr != null) {
            requestDto.setRequestBody(reqBodyAttr.toString());
        }
        requestDto.setRequestBody(reqBodyAttr);
        HttpHeaderBuilder.extractRequestParam(request, requestDto);
        HttpHeaderBuilder.getUrl(requestDto, request);

        String requestId = MDC.get(HeaderEnum.REQUEST_ID.getValue());
        String accessToken = MDC.get(HeaderEnum.ACCESS_TOKEN.getValue());
        String username = MDC.get(HeaderEnum.USERNAME.getValue());
        String method = MDC.get(HeaderEnum.METHOD.getValue());
        String endpoint = MDC.get(HeaderEnum.REQUEST_ENDPOINT.getValue());
        String forwardedFor = MDC.get(HeaderEnum.FORWARDED_FOR.getValue());
        String packageInfo = MDC.get(HeaderEnum.PACKAGE_INFO.getValue());
        String requestBody = requestDto.toString();
        String userAgent = MDC.get(HeaderEnum.USER_AGENT.getValue());
        String responseTime = MDC.get(HeaderEnum.RESPONSE_TIME.getValue());

        String exception = request.getAttribute(HeaderEnum.EXCEPTION.getValue()) != null
                ? request.getAttribute(HeaderEnum.EXCEPTION.getValue()).toString()
                : null;
        String responseService = request.getAttribute(HeaderEnum.RESPONSE.getValue()) != null
                ? request.getAttribute(HeaderEnum.RESPONSE.getValue()).toString()
                : null;

        String methodName = methodParameter.getMethod().getName();
        log.infoServiceEnd(methodName);

        MDC.put(HeaderEnum.SUMMARY_LOG.getValue(), "AFTER COMPLETION");
        int statusCode = response != null ? response.getStatus() : 200;
        log.infoAspectLog(
                requestId, accessToken, username, method, statusCode, endpoint,
                forwardedFor, packageInfo, exception, requestBody, responseService, userAgent, responseTime
        );

        MDC.clear();
    }
}

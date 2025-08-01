package reyga.starter.foundation.logging.interceptor;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.logging.CustomLogger;
import reyga.starter.foundation.common.logging.HttpHeaderBuilder;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LogInterceptor extends BaseLogging implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        Map<String, String> headersMap = HttpHeaderBuilder.buildHeadersMap(request);

        // Tetap isi headersMap
        headersMap.computeIfAbsent(HeaderEnum.REQUEST_ID.getValue(), k -> UUID.randomUUID().toString());
        headersMap.put(HeaderEnum.METHOD.getValue(), request.getMethod());
        headersMap.put(HeaderEnum.REQUEST_ENDPOINT.getValue(), request.getRequestURI());
        headersMap.put(HeaderEnum.FORWARDED_FOR.getValue(), request.getRemoteAddr());
        headersMap.put(HeaderEnum.ACCESS_TOKEN.getValue(), request.getHeader(HeaderEnum.ACCESS_TOKEN.getValue()));
        headersMap.put(HeaderEnum.USERNAME.getValue(), request.getHeader(HeaderEnum.USERNAME.getValue()));
        headersMap.put(HeaderEnum.REQUEST.getValue(), MDC.get(HeaderEnum.REQUEST.getValue()));
        headersMap.put(HeaderEnum.USER_AGENT.getValue(), request.getHeader(HeaderEnum.USER_AGENT.getValue()));

        // Manual MDC.put dan setAttribute
        String requestId     = headersMap.get(HeaderEnum.REQUEST_ID.getValue());
        String method        = headersMap.get(HeaderEnum.METHOD.getValue());
        String endpoint      = headersMap.get(HeaderEnum.REQUEST_ENDPOINT.getValue());
        String forwardedFor  = headersMap.get(HeaderEnum.FORWARDED_FOR.getValue());
        String accessToken   = headersMap.get(HeaderEnum.ACCESS_TOKEN.getValue());
        String username      = headersMap.get(HeaderEnum.USERNAME.getValue());
        String requestBody   = headersMap.get(HeaderEnum.REQUEST.getValue());
        String userAgent     = headersMap.get(HeaderEnum.USER_AGENT.getValue());

        if (requestId != null) {
            MDC.put(HeaderEnum.REQUEST_ID.getValue(), requestId);
            request.setAttribute(HeaderEnum.REQUEST_ID.getValue(), requestId);
        }
        if (method != null) {
            MDC.put(HeaderEnum.METHOD.getValue(), method);
            request.setAttribute(HeaderEnum.METHOD.getValue(), method);
        }
        if (endpoint != null) {
            MDC.put(HeaderEnum.REQUEST_ENDPOINT.getValue(), endpoint);
            request.setAttribute(HeaderEnum.REQUEST_ENDPOINT.getValue(), endpoint);
        }
        if (forwardedFor != null) {
            MDC.put(HeaderEnum.FORWARDED_FOR.getValue(), forwardedFor);
            request.setAttribute(HeaderEnum.FORWARDED_FOR.getValue(), forwardedFor);
        }
        if (accessToken != null) {
            MDC.put(HeaderEnum.ACCESS_TOKEN.getValue(), accessToken);
            request.setAttribute(HeaderEnum.ACCESS_TOKEN.getValue(), accessToken);
        }
        if (username != null) {
            MDC.put(HeaderEnum.USERNAME.getValue(), username);
            request.setAttribute(HeaderEnum.USERNAME.getValue(), username);
        }
        if (requestBody != null) {
            MDC.put(HeaderEnum.REQUEST.getValue(), requestBody);
            request.setAttribute(HeaderEnum.REQUEST.getValue(), requestBody);
        }
        if (userAgent != null) {
            MDC.put(HeaderEnum.USER_AGENT.getValue(), userAgent);
            request.setAttribute(HeaderEnum.USER_AGENT.getValue(), userAgent);
        }

        log.info(headersMap.toString());
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        if (DispatcherType.REQUEST.name().equals(request.getDispatcherType().name()) && handler instanceof HandlerMethod) {
            MDC.put(HeaderEnum.SUMMARY_LOG.getValue(), "AFTER COMPLETION");
            String requestId      = MDC.get(HeaderEnum.REQUEST_ID.getValue());
            String accessToken    = MDC.get(HeaderEnum.ACCESS_TOKEN.getValue());
            String username       = MDC.get(HeaderEnum.USERNAME.getValue());
            String method         = MDC.get(HeaderEnum.METHOD.getValue());
            String endpoint       = MDC.get(HeaderEnum.REQUEST_ENDPOINT.getValue());
            String forwardedFor   = MDC.get(HeaderEnum.FORWARDED_FOR.getValue());
            String packageInfo    = MDC.get(HeaderEnum.PACKAGE_INFO.getValue());
            String requestBody    = MDC.get(HeaderEnum.REQUEST.getValue());
            String userAgent      = MDC.get(HeaderEnum.USER_AGENT.getValue());
            String responseTime   = MDC.get(HeaderEnum.RESPONSE_TIME.getValue());

            String exception = request.getAttribute(HeaderEnum.EXCEPTION.getValue()) != null
                    ? request.getAttribute(HeaderEnum.EXCEPTION.getValue()).toString()
                    : null;
            String responseService = request.getAttribute(HeaderEnum.RESPONSE.getValue()) != null
                    ? request.getAttribute(HeaderEnum.RESPONSE.getValue()).toString()
                    : null;

            log.infoAspectLog(
                    requestId, accessToken, username, method, response.getStatus(), endpoint,
                    forwardedFor, packageInfo, exception, requestBody, responseService, userAgent, responseTime
            );
            MDC.clear();
        }
    }
}

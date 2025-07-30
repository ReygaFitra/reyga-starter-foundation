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
import reyga.starter.foundation.common.logging.CustomLogger;
import reyga.starter.foundation.common.logging.HttpHeaderBuilder;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LogInterceptor implements HandlerInterceptor {

    private final CustomLogger logger;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        Map<String, String> headersMap = HttpHeaderBuilder.buildHeadersMap(request);
        if (headersMap.get(HeaderEnum.REQUEST_ID.getValue()) == null) {
            headersMap.put(HeaderEnum.REQUEST_ID.getValue(), UUID.randomUUID().toString());
        }
        headersMap.put(HeaderEnum.METHOD.getValue(), request.getMethod());
        headersMap.put(HeaderEnum.REQUEST_ENDPOINT.getValue(), request.getRequestURI());
        headersMap.put(HeaderEnum.FORWARDED_FOR.getValue(), request.getRemoteAddr());
        headersMap.put(HeaderEnum.ACCESS_TOKEN.getValue(), request.getHeader(HeaderEnum.ACCESS_TOKEN.getValue()) == null ? null : request.getHeader(HeaderEnum.ACCESS_TOKEN.getValue()));
        headersMap.put(HeaderEnum.USERNAME.getValue(), request.getHeader(HeaderEnum.USERNAME.getValue()) == null ? null : request.getHeader(HeaderEnum.USERNAME.getValue()));
        headersMap.put(HeaderEnum.REQUEST.getValue(), MDC.get(HeaderEnum.REQUEST.getValue()));
        headersMap.put(HeaderEnum.USER_AGENT.getValue(), request.getHeader(HeaderEnum.USER_AGENT.getValue()));
        this.logger.info(headersMap.toString());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        if (DispatcherType.REQUEST.name().equals(request.getDispatcherType().name()) && handler instanceof HandlerMethod) {
            MDC.put(HeaderEnum.SUMMARY_LOG.getValue(), "AFTER COMPLETION");
            this.logger.infoAspectLog(
                    MDC.get(HeaderEnum.ACCESS_TOKEN.getValue()), MDC.get(HeaderEnum.USERNAME.getValue()), MDC.get(HeaderEnum.REQUEST_ID.getValue()),
                    MDC.get(HeaderEnum.METHOD.getValue()), response.getStatus(), MDC.get(HeaderEnum.REQUEST_ENDPOINT.getValue()),
                    MDC.get(HeaderEnum.FORWARDED_FOR.getValue()), MDC.get(HeaderEnum.PACKAGE_INFO.getValue()), request.getAttribute(HeaderEnum.EXCEPTION.getValue()).toString(),
                    MDC.get(HeaderEnum.REQUEST.getValue()), request.getAttribute(HeaderEnum.RESPONSE.getValue()).toString(), MDC.get(HeaderEnum.USER_AGENT.getValue()), MDC.get(HeaderEnum.RESPONSE_TIME.getValue())
            );
            MDC.clear();
        }
    }
}

package reyga.starter.foundation.logging.interceptor;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.logging.HttpHeaderBuilder;
import reyga.starter.foundation.common.model.dto.request.RequestLogging;

import java.util.Map;
import java.util.UUID;

public class DefaultLogInterceptor extends BaseLogInterceptor {

    @Override
    public boolean preHandleProcess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        Map<String, String> headersMap = HttpHeaderBuilder.buildHeadersMap(request);
        request.setAttribute("reqStartTime", System.currentTimeMillis());

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

        if (handler instanceof HandlerMethod handlerMethod) {
            String methodName = handlerMethod.getMethod().getName();
            log.infoServiceStart(methodName);
        }

        log.info(headersMap.toString());
        return true;
    }

    @Override
    public void postHandleProcess(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletionProcess(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
        if (DispatcherType.REQUEST.name().equals(request.getDispatcherType().name()) && handler instanceof HandlerMethod handlerMethod) {
            Long startTime = (Long) request.getAttribute("reqStartTime");
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                MDC.put(HeaderEnum.RESPONSE_TIME.getValue(), duration + " ms");
                request.setAttribute(HeaderEnum.RESPONSE_TIME.getValue(), duration + " ms");
            }

            RequestLogging requestDto = new RequestLogging();
            Object reqBodyAttr = request.getAttribute("LOGGED_REQUEST_BODY");
            if (reqBodyAttr != null) {
                requestDto.setRequestBody(reqBodyAttr.toString());
            }
            requestDto.setRequestBody(reqBodyAttr);
            HttpHeaderBuilder.extractRequestParam(request, requestDto);
            HttpHeaderBuilder.getUrl(requestDto, request);
            MDC.put(HeaderEnum.REQUEST.getValue(), requestDto.toString());

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

            String methodName = handlerMethod.getMethod().getName();
            log.infoServiceEnd(methodName);

            MDC.put(HeaderEnum.SUMMARY_LOG.getValue(), "AFTER COMPLETION");
            log.infoAspectLog(
                    requestId, accessToken, username, method, response.getStatus(), endpoint,
                    forwardedFor, packageInfo, exception, requestBody, responseService, userAgent, responseTime
            );

            MDC.clear();
        }
    }
}

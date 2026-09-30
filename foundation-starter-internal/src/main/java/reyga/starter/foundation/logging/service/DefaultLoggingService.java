package reyga.starter.foundation.logging.service;

import java.util.Map;
import java.util.UUID;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import reyga.starter.foundation.common.enumeration.StarterHeaderEnum;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.HttpHeaderBuilder;
import reyga.starter.foundation.common.logging.InjectLogger;
import reyga.starter.foundation.common.logging.ServiceExecutionSummary;
import reyga.starter.foundation.common.model.dto.request.RequestLogging;

/**
 * Default Spring MVC interceptor that records one HTTP execution summary per request.
 *
 * <p>The interceptor stores request metadata in MDC during {@link #preHandle} and
 * emits the summary during {@link #afterCompletion}. It does not consume or cache
 * request and response bodies. Instances are stateless; request-specific state remains
 * in request attributes and the current thread's MDC.</p>
 */
public class DefaultLoggingService implements LoggingService {

    private static final String DURATION_TIME_ATTRIBUTE = "x-starter-duration-time";

    /** Logger injected by the foundation logger post-processor. */
    @InjectLogger
    protected CommonLogger logger;

    /**
     * Creates the default HTTP logging interceptor.
     */
    public DefaultLoggingService() {
    }

    /**
     * Initializes the MDC and request attributes used by the completion summary.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param handler selected Spring MVC handler
     * @return {@code true} so request processing continues
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (!DispatcherType.REQUEST.equals(request.getDispatcherType())) {
            return true;
        }

        Map<String, String> headersMap = HttpHeaderBuilder.buildHeadersMap(request);
        request.setAttribute(DURATION_TIME_ATTRIBUTE, System.currentTimeMillis());

        headersMap.computeIfAbsent(StarterHeaderEnum.REQUEST_ID.getValue(), k -> UUID.randomUUID().toString());
        headersMap.put(StarterHeaderEnum.METHOD.getValue(), request.getMethod());
        headersMap.put(StarterHeaderEnum.REQUEST_ENDPOINT.getValue(), request.getRequestURI());
        headersMap.put(StarterHeaderEnum.FORWARDED_FOR.getValue(), request.getRemoteAddr());
        headersMap.put(StarterHeaderEnum.USERNAME.getValue(), request.getHeader(StarterHeaderEnum.USERNAME.getValue()));
        headersMap.put(StarterHeaderEnum.USER_AGENT.getValue(), request.getHeader(StarterHeaderEnum.USER_AGENT.getValue()));

        headersMap.forEach((key, value) -> {
            if (value != null) {
                MDC.put(key, value);
                request.setAttribute(key, value);
            }
        });
        return true;
    }

    /**
     * Emits one summary after Spring MVC completes request processing and always clears
     * the MDC associated with the current thread.
     *
     * @param request completed HTTP request
     * @param response completed HTTP response
     * @param handler selected Spring MVC handler
     * @param exception unhandled request-processing failure, or {@code null}
     */
    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            @Nullable Exception exception
    ) {
        if (!DispatcherType.REQUEST.equals(request.getDispatcherType())) {
            return;
        }
        try {
            updateResponseTime(request);
            RequestLogging requestDto = buildRequestSummary(request);

            if (logger != null) {
                String error = exception != null
                        ? exception.toString()
                        : attributeToString(request, StarterHeaderEnum.EXCEPTION);

                MDC.put(StarterHeaderEnum.SUMMARY_LOG.getValue(), "AFTER COMPLETION");
                logger.infoServiceSummary(new ServiceExecutionSummary(
                        handlerName(handler),
                        attributeToString(request, StarterHeaderEnum.REQUEST_ID),
                        attributeToString(request, StarterHeaderEnum.USERNAME),
                        attributeToString(request, StarterHeaderEnum.METHOD),
                        response.getStatus(),
                        attributeToString(request, StarterHeaderEnum.REQUEST_ENDPOINT),
                        attributeToString(request, StarterHeaderEnum.FORWARDED_FOR),
                        attributeToString(request, StarterHeaderEnum.USER_AGENT),
                        attributeToString(request, StarterHeaderEnum.RESPONSE_TIME),
                        requestDto.toString(),
                        null,
                        error
                ));
            }
        } finally {
            MDC.clear();
        }
    }

    private static void updateResponseTime(HttpServletRequest request) {
        Long startTime = (Long) request.getAttribute(DURATION_TIME_ATTRIBUTE);
        if (startTime != null) {
            String duration = System.currentTimeMillis() - startTime + " ms";
            MDC.put(StarterHeaderEnum.RESPONSE_TIME.getValue(), duration);
            request.setAttribute(StarterHeaderEnum.RESPONSE_TIME.getValue(), duration);
        }
    }

    private static RequestLogging buildRequestSummary(HttpServletRequest request) {
        RequestLogging requestDto = new RequestLogging();
        HttpHeaderBuilder.extractRequestParam(request, requestDto);
        HttpHeaderBuilder.getUrl(requestDto, request);
        return requestDto;
    }

    private static String handlerName(Object handler) {
        if (handler instanceof HandlerMethod handlerMethod) {
            return handlerMethod.getBeanType().getName()
                    + "#" + handlerMethod.getMethod().getName();
        }
        return handler.getClass().getName();
    }

    private static String attributeToString(
            HttpServletRequest request,
            StarterHeaderEnum attribute
    ) {
        return objectToString(request.getAttribute(attribute.getValue()));
    }

    private static String objectToString(Object value) {
        return value != null ? value.toString() : null;
    }
}

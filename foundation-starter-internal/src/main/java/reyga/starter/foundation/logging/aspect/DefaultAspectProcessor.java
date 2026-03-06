package reyga.starter.foundation.logging.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StopWatch;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.logging.HttpHeaderBuilder;
import reyga.starter.foundation.common.model.dto.request.RequestLogging;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class DefaultAspectProcessor extends BaseLogging implements BaseAspectProcessor  {

    @Override
    public Object process(ProceedingJoinPoint joinPoint, Method method, HttpServletRequest request, HttpServletResponse response) throws Throwable {
        Map<String, String> headersMap = HttpHeaderBuilder.buildHeadersMap(request);
        StopWatch stopWatch = new StopWatch();

        RequestLogging requestDto = new RequestLogging();
        HttpHeaderBuilder.constructRequestBodyAndRequestMultiPart(joinPoint.getArgs(), method, requestDto);
        HttpHeaderBuilder.extractRequestParam(request, requestDto);
        HttpHeaderBuilder.getUrl(requestDto, request);

        headersMap.computeIfAbsent(HeaderEnum.REQUEST_ID.getValue(), k -> UUID.randomUUID().toString());
        headersMap.put(HeaderEnum.METHOD.getValue(), request.getMethod());
        headersMap.put(HeaderEnum.REQUEST_ENDPOINT.getValue(), request.getRequestURI());
        headersMap.put(HeaderEnum.FORWARDED_FOR.getValue(), request.getRemoteAddr());
        headersMap.put(HeaderEnum.ACCESS_TOKEN.getValue(), request.getHeader(HeaderEnum.ACCESS_TOKEN.getValue()));
        headersMap.put(HeaderEnum.USERNAME.getValue(), request.getHeader(HeaderEnum.USERNAME.getValue()));
        headersMap.put(HeaderEnum.USER_AGENT.getValue(), request.getHeader(HeaderEnum.USER_AGENT.getValue()));
        headersMap.put(HeaderEnum.REQUEST.getValue(), requestDto.toString());
        headersMap.put(HeaderEnum.PACKAGE_INFO.getValue(), joinPoint.getTarget().getClass().getName());

        try {
            stopWatch.start();
            log.infoServiceStart(joinPoint.getSignature().getName());
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();

            String requestId      = headersMap.get(HeaderEnum.REQUEST_ID.getValue());
            String accessToken    = headersMap.get(HeaderEnum.ACCESS_TOKEN.getValue());
            String username       = headersMap.get(HeaderEnum.USERNAME.getValue());
            String headersMethod         = headersMap.get(HeaderEnum.METHOD.getValue());
            String endpoint       = headersMap.get(HeaderEnum.REQUEST_ENDPOINT.getValue());
            String forwardedFor   = headersMap.get(HeaderEnum.FORWARDED_FOR.getValue());
            String packageInfo    = headersMap.get(HeaderEnum.PACKAGE_INFO.getValue());
            String requestBody    = headersMap.get(HeaderEnum.REQUEST.getValue());
            String userAgent      = headersMap.get(HeaderEnum.USER_AGENT.getValue());
            String responseTime   = executionTime + "ms";

            String exception = request.getAttribute(HeaderEnum.EXCEPTION.getValue()) != null
                    ? request.getAttribute(HeaderEnum.EXCEPTION.getValue()).toString()
                    : null;
            String responseService = request.getAttribute(HeaderEnum.RESPONSE.getValue()) != null
                    ? request.getAttribute(HeaderEnum.RESPONSE.getValue()).toString()
                    : null;

            log.infoAspectLog(
                    requestId, accessToken, username, headersMethod, response.getStatus(), endpoint,
                    forwardedFor, packageInfo, exception, requestBody, responseService, userAgent, responseTime
            );

            log.infoServiceEnd(joinPoint.getSignature().getName());
        }
    }

}
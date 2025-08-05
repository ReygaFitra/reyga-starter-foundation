package reyga.starter.foundation.logging.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reyga.starter.foundation.common.enumeration.HeaderEnum;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.common.logging.HttpHeaderBuilder;
import reyga.starter.foundation.common.model.dto.request.RequestLogging;
import reyga.starter.foundation.logging.config.properties.LoggingProperties;

import java.lang.reflect.Method;
import java.util.UUID;

@Aspect
@RequiredArgsConstructor
public class AspectLogging extends BaseLogging {

    private final LoggingProperties loggingProperties;

    @Pointcut(value = "@annotation(reyga.starter.foundation.logging.annotation.AspectLogExecution)")
    private void pointCut(){
    };

    @Around(value = "pointCut()")
    public Object processRequestIntercept(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        if (requestAttributes != null) {
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
        }
        if (!loggingProperties.isEnableAspectLogging()) {
            return joinPoint.proceed();
        }

        if (MDC.get(HeaderEnum.REQUEST_ID.getValue()) == null) {
            MDC.put(HeaderEnum.REQUEST_ID.getValue(), UUID.randomUUID().toString());
        }
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequestLogging requestDto = new RequestLogging();
        HttpHeaderBuilder.constructRequestBodyAndRequestMultiPart(joinPoint.getArgs(), method, requestDto);
        HttpHeaderBuilder.extractRequestParam(request, requestDto);
        HttpHeaderBuilder.getUrl(requestDto, request);

        MDC.put(HeaderEnum.REQUEST.getValue(), requestDto.toString());
        try {
            stopWatch.start();
            log.infoServiceStart(joinPoint.getSignature().getName());
            return joinPoint.proceed();
        } finally {
            stopWatch.stop();
            long executionTime = stopWatch.getTotalTimeMillis();
            MDC.put(HeaderEnum.RESPONSE_TIME.getValue(), executionTime + " ms");
            MDC.put(HeaderEnum.PACKAGE_INFO.getValue(), joinPoint.getTarget().getClass().getName());
            log.infoServiceEnd(joinPoint.getSignature().getName());
        }
    }
}

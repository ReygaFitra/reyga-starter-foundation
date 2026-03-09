package reyga.starter.foundation.logging.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reyga.starter.foundation.logging.config.properties.LoggingProperties;

import java.lang.reflect.Method;

@Aspect
@RequiredArgsConstructor
public class AspectLogging {

    private final LoggingProperties loggingProperties;
    private final BaseAspectProcessor baseAspectProcessor;

    @Pointcut(value = "@annotation(reyga.starter.foundation.logging.annotation.AspectLogExecution)")
    private void pointCut(){
    };

    @Around(value = "pointCut()")
    public Object processRequestIntercept(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!loggingProperties.enableAspectLogging()) {
            return joinPoint.proceed();
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        HttpServletResponse response = null;

        if (requestAttributes instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) requestAttributes).getRequest();
            response = ((ServletRequestAttributes) requestAttributes).getResponse();
        }

        return baseAspectProcessor.process(joinPoint, method, request, response);
    }
}

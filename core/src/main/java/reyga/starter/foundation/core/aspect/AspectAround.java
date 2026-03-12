package reyga.starter.foundation.core.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
@RequiredArgsConstructor
public class AspectAround {

    private final boolean enableAspectLogging;
    private final AspectProcessor aspectProcessor;

    @Pointcut(value = "@annotation(reyga.starter.foundation.core.annotation.AroundExecution)")
    private void pointCut() {
    }

    @Around(value = "pointCut()")
    public Object processRequestIntercept(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enableAspectLogging) {
            return joinPoint.proceed();
        }

        return aspectProcessor.process(joinPoint);
    }
}

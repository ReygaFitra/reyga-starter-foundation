package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.Objects;

/**
 * Dispatches methods annotated with {@code @AroundExecution} to the configured
 * {@link BaseAspectAround} behavior.
 *
 * <p>The starter creates this aspect only when
 * {@code reyga.config.aspect.around=true}. The behavior itself is selected by
 * its Spring bean name through {@code reyga.config.aspect.behavior}.</p>
 */
@Aspect
public class AspectAround {

    private final AspectProcessor aspectProcessor;

    /**
     * Creates the dispatcher for the selected behavior.
     *
     * @param aspectProcessor behavior invoked for every intercepted method
     */
    public AspectAround(AspectProcessor aspectProcessor) {
        this.aspectProcessor = Objects.requireNonNull(aspectProcessor, "aspectProcessor must not be null");
    }

    @Pointcut(value = "@annotation(reyga.starter.foundation.core.annotation.AroundExecution)")
    private void pointCut() {
    }

    /**
     * Routes an intercepted invocation through the selected behavior.
     *
     * @param joinPoint intercepted method invocation
     * @return value returned by the behavior
     * @throws Throwable when the behavior or target method fails
     */
    @Around(value = "pointCut()")
    public Object processRequestIntercept(ProceedingJoinPoint joinPoint) throws Throwable {
        return aspectProcessor.process(joinPoint);
    }
}

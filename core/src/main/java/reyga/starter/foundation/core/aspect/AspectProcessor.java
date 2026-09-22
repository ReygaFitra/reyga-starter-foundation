package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Contract used by the around-aspect dispatcher to process an intercepted
 * method invocation.
 */
@FunctionalInterface
public interface AspectProcessor {

    /**
     * Processes an intercepted invocation.
     *
     * @param joinPoint intercepted method invocation
     * @return value returned to the original caller
     * @throws Throwable when the behavior or intercepted method fails
     */
    Object process(ProceedingJoinPoint joinPoint) throws Throwable;
}

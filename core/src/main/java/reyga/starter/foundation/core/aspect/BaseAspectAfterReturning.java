package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.JoinPoint;

/**
 * Base behavior executed after a method annotated with
 * {@code @AfterReturningExecution} returns successfully.
 */
public abstract class BaseAspectAfterReturning {

    /**
     * Creates an after-returning-advice behavior.
     */
    protected BaseAspectAfterReturning() {
    }

    /**
     * Dispatches the intercepted invocation and return value to the consumer
     * hook.
     *
     * @param joinPoint intercepted method invocation
     * @param result value returned by the target method, possibly {@code null}
     */
    public final void process(JoinPoint joinPoint, Object result) {
        afterReturningHandle(joinPoint, result);
    }

    /**
     * Runs after the target method returns successfully.
     *
     * @param joinPoint intercepted method invocation
     * @param result value returned by the target method, possibly {@code null}
     */
    protected abstract void afterReturningHandle(JoinPoint joinPoint, Object result);
}

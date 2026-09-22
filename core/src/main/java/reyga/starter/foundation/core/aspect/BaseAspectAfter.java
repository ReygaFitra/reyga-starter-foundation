package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.JoinPoint;

/**
 * Base behavior executed after a method annotated with {@code @AfterExecution},
 * whether the method succeeds or fails.
 */
public abstract class BaseAspectAfter {

    /**
     * Creates an after-advice behavior.
     */
    protected BaseAspectAfter() {
    }

    /**
     * Dispatches the intercepted invocation to the consumer hook.
     *
     * @param joinPoint intercepted method invocation
     */
    public final void process(JoinPoint joinPoint) {
        afterHandle(joinPoint);
    }

    /**
     * Runs after the target method completes.
     *
     * @param joinPoint intercepted method invocation
     */
    protected abstract void afterHandle(JoinPoint joinPoint);
}

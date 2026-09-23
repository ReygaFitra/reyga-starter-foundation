package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.JoinPoint;

/**
 * Base behavior executed after a method annotated with
 * {@code @AfterThrowingExecution} throws an error or exception.
 */
public abstract class BaseAspectAfterThrowing {

    /**
     * Creates an after-throwing-advice behavior.
     */
    protected BaseAspectAfterThrowing() {
    }

    /**
     * Dispatches the intercepted invocation and failure to the consumer hook.
     *
     * @param joinPoint intercepted method invocation
     * @param throwable failure thrown by the target method
     */
    public final void process(JoinPoint joinPoint, Throwable throwable) {
        afterThrowingHandle(joinPoint, throwable);
    }

    /**
     * Runs after the target method throws.
     *
     * @param joinPoint intercepted method invocation
     * @param throwable failure thrown by the target method
     */
    protected abstract void afterThrowingHandle(JoinPoint joinPoint, Throwable throwable);
}

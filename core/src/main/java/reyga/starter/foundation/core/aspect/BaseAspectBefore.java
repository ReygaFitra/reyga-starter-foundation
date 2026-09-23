package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.JoinPoint;

/**
 * Base behavior executed before a method annotated with
 * {@code @BeforeExecution}.
 */
public abstract class BaseAspectBefore {

    /**
     * Creates a before-advice behavior.
     */
    protected BaseAspectBefore() {
    }

    /**
     * Dispatches the intercepted invocation to the consumer hook.
     *
     * @param joinPoint intercepted method invocation
     */
    public final void process(JoinPoint joinPoint) {
        beforeHandle(joinPoint);
    }

    /**
     * Runs before the target method.
     *
     * @param joinPoint intercepted method invocation
     */
    protected abstract void beforeHandle(JoinPoint joinPoint);
}

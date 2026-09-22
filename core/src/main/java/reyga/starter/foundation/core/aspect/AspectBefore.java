package reyga.starter.foundation.core.aspect;

import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Dispatches methods annotated with {@code @BeforeExecution} to the configured
 * {@link BaseAspectBefore} behavior.
 */
@Aspect
public class AspectBefore {

    private final BaseAspectBefore behavior;

    /**
     * Creates the dispatcher for the selected behavior.
     *
     * @param behavior behavior invoked before intercepted methods
     */
    public AspectBefore(BaseAspectBefore behavior) {
        this.behavior = Objects.requireNonNull(behavior, "behavior must not be null");
    }

    @Pointcut(value = "@annotation(reyga.starter.foundation.core.annotation.BeforeExecution)")
    private void pointCut() {
    }

    /**
     * Routes an intercepted invocation through the selected behavior.
     *
     * @param joinPoint intercepted method invocation
     */
    @Before(value = "pointCut()")
    public void processBeforeIntercept(JoinPoint joinPoint) {
        behavior.process(joinPoint);
    }
}

package reyga.starter.foundation.core.aspect;

import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Dispatches methods annotated with {@code @AfterExecution} to the configured
 * {@link BaseAspectAfter} behavior.
 */
@Aspect
public class AspectAfter {

    private final BaseAspectAfter behavior;

    /**
     * Creates the dispatcher for the selected behavior.
     *
     * @param behavior behavior invoked after intercepted methods
     */
    public AspectAfter(BaseAspectAfter behavior) {
        this.behavior = Objects.requireNonNull(behavior, "behavior must not be null");
    }

    @Pointcut(value = "@annotation(reyga.starter.foundation.core.annotation.AfterExecution)")
    private void pointCut() {
    }

    /**
     * Routes an intercepted invocation through the selected behavior.
     *
     * @param joinPoint intercepted method invocation
     */
    @After(value = "pointCut()")
    public void processAfterIntercept(JoinPoint joinPoint) {
        behavior.process(joinPoint);
    }
}

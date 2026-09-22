package reyga.starter.foundation.core.aspect;

import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Dispatches methods annotated with {@code @AfterReturningExecution} to the
 * configured {@link BaseAspectAfterReturning} behavior.
 */
@Aspect
public class AspectAfterReturning {

    private final BaseAspectAfterReturning behavior;

    /**
     * Creates the dispatcher for the selected behavior.
     *
     * @param behavior behavior invoked after successful methods
     */
    public AspectAfterReturning(BaseAspectAfterReturning behavior) {
        this.behavior = Objects.requireNonNull(behavior, "behavior must not be null");
    }

    @Pointcut(value = "@annotation(reyga.starter.foundation.core.annotation.AfterReturningExecution)")
    private void pointCut() {
    }

    /**
     * Routes an intercepted invocation and its return value through the selected
     * behavior.
     *
     * @param joinPoint intercepted method invocation
     * @param result value returned by the target method, possibly {@code null}
     */
    @AfterReturning(value = "pointCut()", returning = "result")
    public void processAfterReturningIntercept(JoinPoint joinPoint, Object result) {
        behavior.process(joinPoint, result);
    }
}

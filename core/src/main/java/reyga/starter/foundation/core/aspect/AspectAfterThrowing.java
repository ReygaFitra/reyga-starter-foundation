package reyga.starter.foundation.core.aspect;

import java.util.Objects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Dispatches methods annotated with {@code @AfterThrowingExecution} to the
 * configured {@link BaseAspectAfterThrowing} behavior.
 */
@Aspect
public class AspectAfterThrowing {

    private final BaseAspectAfterThrowing behavior;

    /**
     * Creates the dispatcher for the selected behavior.
     *
     * @param behavior behavior invoked after failed methods
     */
    public AspectAfterThrowing(BaseAspectAfterThrowing behavior) {
        this.behavior = Objects.requireNonNull(behavior, "behavior must not be null");
    }

    @Pointcut(value = "@annotation(reyga.starter.foundation.core.annotation.AfterThrowingExecution)")
    private void pointCut() {
    }

    /**
     * Routes an intercepted invocation and its failure through the selected
     * behavior.
     *
     * @param joinPoint intercepted method invocation
     * @param throwable failure thrown by the target method
     */
    @AfterThrowing(value = "pointCut()", throwing = "throwable")
    public void processAfterThrowingIntercept(JoinPoint joinPoint, Throwable throwable) {
        behavior.process(joinPoint, throwable);
    }
}

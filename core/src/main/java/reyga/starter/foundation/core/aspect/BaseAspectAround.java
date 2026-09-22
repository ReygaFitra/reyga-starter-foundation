package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import reyga.starter.foundation.common.model.dto.content.BaseContent;

/**
 * Base behavior for methods intercepted by {@code @AroundExecution}.
 *
 * <p>Consumer applications should extend this class, register the subclass as
 * a Spring component, and configure its bean name through
 * {@code reyga.config.aspect.behavior}.</p>
 *
 * @param <C> invocation context shared by the lifecycle hooks
 */
public abstract class BaseAspectAround<C extends BaseContent> implements AspectProcessor {

    /**
     * Creates a behavior implementation.
     */
    protected BaseAspectAround() {
    }

    @Override
    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {

        C content = preHandle();

        try {

            return joinPoint.proceed();

        } catch (Exception e) {

            if (shouldHandleException()) {

                handleException(e, content);

            }

            throw e;

        } finally {

            postHandle(content);

        }
    }


    /**
     * Creates or initializes context before the target method runs.
     *
     * @return context forwarded to the remaining lifecycle hooks
     */
    protected abstract C preHandle();

    /**
     * Runs after the target invocation, including when it throws an exception.
     *
     * @param content context returned by {@link #preHandle()}
     */
    protected abstract void postHandle(C content);

    /**
     * Determines whether {@link #handleException(Exception, BaseContent)} should
     * run when the target method throws an exception.
     *
     * @return {@code true} to invoke the exception hook
     */
    protected abstract boolean shouldHandleException();

    /**
     * Handles an exception before it is rethrown to the original caller.
     *
     * @param e exception thrown by the target method
     * @param content context returned by {@link #preHandle()}
     */
    protected void handleException(Exception e, C content) {}

}

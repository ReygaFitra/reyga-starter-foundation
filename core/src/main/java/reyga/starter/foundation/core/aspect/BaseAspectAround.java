package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import reyga.starter.foundation.common.logging.BaseLogging;
import reyga.starter.foundation.core.dto.content.BaseContent;

public abstract class BaseAspectAround<C extends BaseContent> extends BaseLogging implements AspectProcessor {

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


    protected abstract C preHandle();

    protected abstract void postHandle(C content);

    protected boolean shouldHandleException() {
        return false;
    }

    protected void handleException(Exception e, C content) {}


}

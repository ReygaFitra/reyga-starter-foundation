package reyga.starter.foundation.core.aspect;

import org.aspectj.lang.ProceedingJoinPoint;

public interface AspectProcessor {
    Object process(ProceedingJoinPoint joinPoint) throws Throwable;
}

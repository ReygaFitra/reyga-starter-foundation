package reyga.starter.foundation.logging.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;

public interface BaseAspectProcessor {
    Object process(ProceedingJoinPoint joinPoint, Method method, HttpServletRequest request, HttpServletResponse response) throws Throwable;
}

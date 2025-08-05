package reyga.starter.foundation.logging.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import reyga.starter.foundation.common.logging.BaseLogging;;


public abstract class BaseLogInterceptor extends BaseLogging implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        return preHandleProcess(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        postHandleProcess(request, response, handler, modelAndView);
    }


    @Override
    public void afterCompletion(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) throws Exception {
        afterCompletionProcess(request, response, handler, ex);
    }

    protected boolean preHandleProcess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        return true;
    }

    protected void postHandleProcess(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) {
    }

    protected void afterCompletionProcess(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
    }

}

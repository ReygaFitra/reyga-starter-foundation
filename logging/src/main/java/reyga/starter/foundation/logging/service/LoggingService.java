package reyga.starter.foundation.logging.service;

import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts Spring MVC requests to maintain logging context and emit completion
 * summaries.
 *
 * <p>Implementations participate in the standard {@link HandlerInterceptor}
 * lifecycle and must clear thread-bound diagnostic context after completion.</p>
 */
public interface LoggingService extends HandlerInterceptor {
}

package reyga.starter.foundation.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "reyga.config")
public record ConfigProperties(
        @DefaultValue Default defaultBean,
        @DefaultValue RateLimiter rateLimiter,
        @DefaultValue CircuitBreaker circuitBreaker,
        @DefaultValue Retry retry
) {

    public record Default(
            @DefaultValue("false") boolean exceptionHandler,
            @DefaultValue("false") boolean validationHandler,
            @DefaultValue("false") boolean loggingHandler,
            @DefaultValue("false") boolean requestResponseAdvice,
            @DefaultValue("false") boolean aspectHandler,
            @DefaultValue("false") boolean utilities
    ) {
    }

    public record RateLimiter(
            @DefaultValue("false") boolean required,
            @DefaultValue("60000") int timeoutMilis,
            @DefaultValue("3") int maxRequest,
            @DefaultValue("6000") int refreshPeriodSeconds
    ) {
    }

    public record CircuitBreaker(
            @DefaultValue("false") boolean required,
            float failureRateThreshold,
            float slowCallRateThreshold,
            long slowCallDurationThresholdSeconds,
            int minimumNumberOfCalls,
            int slidingWindowSize,
            int permittedNumberOfCallsInHalfOpenState,
            long waitDurationInOpenStateSeconds,
            boolean automaticTransitionFromOpenToHalfOpenEnabled
    ) {
    }

    public record Retry(
            @DefaultValue("false") boolean required,
            @DefaultValue("3") int maxAttempts,
            @DefaultValue("10000") long waitDurationMillis,
            @DefaultValue("true") boolean failAfterMaxAttempts
    ) {
    }
}

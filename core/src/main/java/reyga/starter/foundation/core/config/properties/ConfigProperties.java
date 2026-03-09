package reyga.starter.foundation.core.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "reyga.config")
public record ConfigProperties(
        @DefaultValue ConfigException exception,
        @DefaultValue Validation validation,
        @DefaultValue RateLimiter rateLimiter,
        @DefaultValue CircuitBreaker circuitBreaker,
        @DefaultValue Retry retry
) {

    public record ConfigException(@DefaultValue("true") boolean enableDefault) {
    }

    public record Validation(@DefaultValue("true") boolean enableDefault) {
    }

    public record RateLimiter(
            @DefaultValue("false") boolean required,
            @DefaultValue("1") int timeoutMilis,
            @DefaultValue("1") int maxRequest,
            @DefaultValue("1") int refreshPeriodSeconds
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

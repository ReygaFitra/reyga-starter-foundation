package reyga.starter.foundation.core.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "reyga.config")
public class ConfigProperties {

    Exception exception;
    Validation validation;
    RateLimiter rateLimiter;
    CircuitBreaker circuitBreaker;
    LocalCache localCache;

    @Data
    public static class Exception {
        private boolean enableDefault = true;
    }

    @Data
    public static class Validation {
        private boolean enableDefault = true;
    }

    @Data
    public static class RateLimiter {
        private boolean required = false;
        private int timeoutMilis = 1;
        private int maxRequest = 1;
        private int refreshPeriodSeconds = 1;
    }

    @Data
    public static class CircuitBreaker {
        private boolean required = false;
        private float failureRateThreshold;
        private float slowCallRateThreshold;
        private long slowCallDurationThresholdSeconds;
        private int minimumNumberOfCalls;
        private int slidingWindowSize;
        private int permittedNumberOfCallsInHalfOpenState;
        private long waitDurationInOpenStateSeconds;
        private boolean automaticTransitionFromOpenToHalfOpenEnabled;
    }

    @Data
    public static class LocalCache {
        private int expiresMinutes;
        private int maxSize;
    }
}

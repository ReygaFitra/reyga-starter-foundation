package reyga.starter.foundation.core.service;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class DefaultResilienceServiceTest {

    private final DefaultResilienceService service = new DefaultResilienceService();

    @Test
    void useRateLimiter_returnsValueOnSuccess() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(1)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);

        String result = service.useRateLimiter(config, registry, "key", () -> "ok", () -> "fallback");

        assertEquals("ok", result);
    }

    @Test
    void useRateLimiter_usesFallbackOnLimitExceeded() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(0)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);

        String result = service.useRateLimiter(config, registry, "key", () -> "ok", () -> "fallback");

        assertEquals("fallback", result);
    }

    @Test
    void useCircuitBreaker_usesFallbackOnException() {
        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);

        String result = service.useCircuitBreaker(config, registry, "cb", () -> {
            throw new IllegalStateException("fail");
        }, () -> "fallback");

        assertEquals("fallback", result);
    }

    @Test
    void useRetry_returnsValueOnSuccess() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(1)
                .waitDuration(Duration.ofMillis(1))
                .build();
        RetryRegistry registry = RetryRegistry.of(config);

        String result = service.useRetry(config, registry, "retry", () -> "ok");

        assertEquals("ok", result);
    }

    @Test
    void useRetry_throwsWhenProcessFails() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(1)
                .waitDuration(Duration.ofMillis(1))
                .build();
        RetryRegistry registry = RetryRegistry.of(config);

        assertThrows(RuntimeException.class, () ->
                service.useRetry(config, registry, "retry", () -> {
                    throw new RuntimeException("fail");
                })
        );
    }

    @Test
    void useRateLimiterRunnable_usesFallbackOnLimitExceeded() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(0)
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO)
                .build();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);

        AtomicBoolean fallback = new AtomicBoolean(false);
        service.useRateLimiter(config, registry, "key", () -> {}, () -> fallback.set(true));

        assertTrue(fallback.get());
    }
}

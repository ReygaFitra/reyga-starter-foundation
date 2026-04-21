package reyga.starter.foundation.core.service.foundation;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.cache.Cache;

import java.util.function.Supplier;

public interface ResilienceService {

    <S> S useRateLimiter(
            RateLimiterConfig config, RateLimiterRegistry registry, String rateLimitkey,
            Supplier<S> suppliedProcess, Supplier<S> fallbackSuppliedProcess
    );

    <S> S useRateLimiterWithCache(
            RateLimiterConfig config, RateLimiterRegistry registry, Cache cache, String rateLimitkey,
            Supplier<S> suppliedProcess, Supplier<S> fallbackSuppliedProcess
    );

    <S> S useCircuitBreaker(
            CircuitBreakerConfig config, CircuitBreakerRegistry registry, String cbName, Supplier<S> suppliedProcess, Supplier<S> fallbackSuppliedProcess
    );

    <S> S useRetry(RetryConfig config, RetryRegistry registry, String name, Supplier<S> suppliedProcess);

    void useRateLimiter(
            RateLimiterConfig config, RateLimiterRegistry registry, String rateLimitkey,
            Runnable runProcess, Runnable fallbackProcess
    );

    void useRateLimiterWithCache(
            RateLimiterConfig config, RateLimiterRegistry registry, Cache cache, String rateLimitkey,
            Runnable runProcess, Runnable fallbackProcess
    );

    void useCircuitBreaker(
            CircuitBreakerConfig config, CircuitBreakerRegistry registry, String cbName, Runnable runProcess, Runnable fallbackProcess
    );

    void useRetry(RetryConfig config, RetryRegistry registry, String name, Runnable runProcess);

}

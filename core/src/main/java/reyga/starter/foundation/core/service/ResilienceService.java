package reyga.starter.foundation.core.service;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

import java.util.function.Supplier;

public interface ResilienceService {

    <S> S useRateLimiter(
            RateLimiterConfig config, RateLimiterRegistry registry, Cache<String, RateLimiter> localCache, String rateLimitkey,
            Supplier<S> suppliedProcess, Supplier<S> fallbackSuppliedProcess
    );

    <S> S useCircuitBreaker(
            CircuitBreakerConfig config, CircuitBreakerRegistry registry, String cbName, Supplier<S> suppliedProcess, Supplier<S> fallbackSuppliedProcess
    );

    void useRateLimiterVoid(
            RateLimiterConfig config, RateLimiterRegistry registry, Cache<String, RateLimiter> localCache, String rateLimitkey,
            Runnable suppliedProcess, Runnable fallbackSuppliedProcess
    );

    void useCircuitBreakerVoid(
            CircuitBreakerConfig config, CircuitBreakerRegistry registry, String cbName, Runnable suppliedProcess, Runnable fallbackSuppliedProcess
    );

}

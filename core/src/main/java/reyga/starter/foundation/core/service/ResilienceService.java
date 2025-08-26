package reyga.starter.foundation.core.service;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

@Service
public interface ResilienceService {

    default <SP> SP useRateLimiter(
            RateLimiterConfig config, RateLimiterRegistry registry, Cache<String, RateLimiter> localCache, String rateLimitkey,
            Supplier<SP> suppliedProcess, Supplier<SP> fallbackSuppliedProcess
    ) {
        RateLimiter rl =  localCache.get(rateLimitkey, k -> registry.rateLimiter(rateLimitkey, config));
        try {
            return RateLimiter.decorateSupplier(rl, suppliedProcess).get();
        } catch (Exception e) {
            return fallbackSuppliedProcess.get();
        }
    }

    default <SP> SP useCircuitBreaker(
            CircuitBreakerConfig config, CircuitBreakerRegistry registry, String cbName, Supplier<SP> suppliedProcess, Supplier<SP> fallbackSuppliedProcess
    ) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker(cbName, config);
        try {
            return CircuitBreaker.decorateSupplier(circuitBreaker, suppliedProcess).get();
        } catch (Exception e) {
            return fallbackSuppliedProcess.get();
        }
    }

    default void useRateLimiterVoid(
            RateLimiterConfig config, RateLimiterRegistry registry, Cache<String, RateLimiter> localCache, String rateLimitkey,
            Runnable suppliedProcess, Runnable fallbackSuppliedProcess
    ) {
        RateLimiter rl =  localCache.get(rateLimitkey, k -> registry.rateLimiter(rateLimitkey, config));
        try {
            RateLimiter.decorateRunnable(rl, suppliedProcess).run();
        } catch (Exception e) {
            fallbackSuppliedProcess.run();
        }
    }

    default void useCircuitBreakerVoid(
            CircuitBreakerConfig config, CircuitBreakerRegistry registry, String cbName, Runnable suppliedProcess, Runnable fallbackSuppliedProcess
    ) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker(cbName, config);
        try {
            CircuitBreaker.decorateRunnable(circuitBreaker, suppliedProcess).run();
        } catch (Exception e) {
            fallbackSuppliedProcess.run();
        }
    }

}

package reyga.starter.foundation.core.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public interface ResilienceService {

    default <SP> SP useRateLimiter(
            RateLimiterConfig rateLimiterConfig, ConcurrentHashMap<String, RateLimiter> concurrentHashMap, String rateLimitkey, String serviceName,
            Supplier<SP> suppliedProcess, Supplier<SP> fallbackSuppliedProcess
    ) {
        RateLimiterRegistry customRateLimiterRegistry = RateLimiterRegistry.of(rateLimiterConfig);
        RateLimiter rateLimiter = concurrentHashMap.computeIfAbsent(rateLimitkey, id -> customRateLimiterRegistry.rateLimiter(serviceName));
        try {
            return RateLimiter.decorateSupplier(rateLimiter, suppliedProcess).get();
        } catch (Exception e) {
            return fallbackSuppliedProcess.get();
        }
    }

    default <SP> SP useCircuitBreaker(
            CircuitBreakerConfig circuitBreakerConfig, String serviceName, Supplier<SP> suppliedProcess, Supplier<SP> fallbackSuppliedProcess
    ) {
        CircuitBreakerRegistry customCircuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        CircuitBreaker circuitBreaker = customCircuitBreakerRegistry.circuitBreaker(serviceName);
        try {
            return CircuitBreaker.decorateSupplier(circuitBreaker, suppliedProcess).get();
        } catch (Exception e) {
            return fallbackSuppliedProcess.get();
        }
    }

}

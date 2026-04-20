package reyga.starter.foundation.core.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.cache.Cache;
import reyga.starter.foundation.common.logging.CommonLogger;
import reyga.starter.foundation.common.logging.InjectLogger;

import java.util.function.Supplier;

public class DefaultResilienceService implements ResilienceService {

    @InjectLogger
    protected CommonLogger logger;

    @Override
    public <S> S useRateLimiter(
            RateLimiterConfig config, RateLimiterRegistry registry, String rateLimitkey,
            Supplier<S> suppliedProcess, Supplier<S> fallbackSuppliedProcess
    ) {
        RateLimiter rl = registry.rateLimiter(rateLimitkey, config);
        try {
            return RateLimiter.decorateSupplier(rl, suppliedProcess).get();
        } catch (RequestNotPermitted e) {
            this.printRequestNotPermitted(e);
            return fallbackSuppliedProcess.get();
        }
    }

    @Override
    public <S> S useRateLimiterWithCache(
            RateLimiterConfig config, RateLimiterRegistry registry, Cache cache, String rateLimitkey,
            Supplier<S> suppliedProcess, Supplier<S> fallbackSuppliedProcess
    ) {
        RateLimiter rl = resolveRateLimiterWithCache(config, registry, cache, rateLimitkey);
        try {
            return RateLimiter.decorateSupplier(rl, suppliedProcess).get();
        } catch (RequestNotPermitted e) {
            this.printRequestNotPermitted(e);
            return fallbackSuppliedProcess.get();
        }
    }

    @Override
    public <S> S useCircuitBreaker(
            CircuitBreakerConfig config, CircuitBreakerRegistry registry, String cbName, Supplier<S> suppliedProcess, Supplier<S> fallbackSuppliedProcess
    ) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker(cbName, config);
        try {
            return CircuitBreaker.decorateSupplier(circuitBreaker, suppliedProcess).get();
        } catch (Exception _) {
            return fallbackSuppliedProcess.get();
        }
    }

    @Override
    public <S> S useRetry(RetryConfig config, RetryRegistry registry, String name, Supplier<S> suppliedProcess) {
        Retry retry = registry.retry(name, config);
        return retry.decorateSupplier(suppliedProcess).get();
    }

    @Override
    public void useRateLimiter(
            RateLimiterConfig config, RateLimiterRegistry registry, String rateLimitkey,
            Runnable runProcess, Runnable fallbackProcess
    ) {
        RateLimiter rl = registry.rateLimiter(rateLimitkey, config);
        try {
            RateLimiter.decorateRunnable(rl, runProcess).run();
        } catch (RequestNotPermitted e) {
            this.printRequestNotPermitted(e);
            fallbackProcess.run();
        }
    }

    @Override
    public void useRateLimiterWithCache(
            RateLimiterConfig config, RateLimiterRegistry registry, Cache cache, String rateLimitkey,
            Runnable runProcess, Runnable fallbackProcess
    ) {
        RateLimiter rl = resolveRateLimiterWithCache(config, registry, cache, rateLimitkey);
        try {
            RateLimiter.decorateRunnable(rl, runProcess).run();
        } catch (RequestNotPermitted _) {
            fallbackProcess.run();
        }
    }

    @Override
    public void useCircuitBreaker(
            CircuitBreakerConfig config, CircuitBreakerRegistry registry, String cbName, Runnable runProcess, Runnable fallbackProcess
    ) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker(cbName, config);
        try {
            CircuitBreaker.decorateRunnable(circuitBreaker, runProcess).run();
        } catch (Exception _) {
            fallbackProcess.run();
        }
    }

    @Override
    public void useRetry(RetryConfig config, RetryRegistry registry, String name, Runnable process) {
        Retry retry = registry.retry(name, config);
        retry.decorateRunnable(process).run();
    }

    private RateLimiter resolveRateLimiterWithCache(
            RateLimiterConfig config, RateLimiterRegistry registry, Cache cache, String rateLimitkey
    ) {
        try {
            RateLimiter cachedRateLimiter = cache.get(rateLimitkey, RateLimiter.class);
            if (cachedRateLimiter != null) {
                return cachedRateLimiter;
            }
        } catch (RuntimeException _) {
            // Fallback to registry if cache implementation cannot deserialize/store RateLimiter.
        }

        RateLimiter rateLimiter = registry.rateLimiter(rateLimitkey, config);

        try {
            cache.put(rateLimitkey, rateLimiter);
        } catch (RuntimeException _) {
            // Fallback to registry-only behavior if cache put is not supported.
        }

        return rateLimiter;
    }

    private void printRequestNotPermitted(RequestNotPermitted requestNotPermitted) {
        if (logger != null) {
            logger.warn("Request Not Permitted :", requestNotPermitted.getMessage());
        }
    }

}

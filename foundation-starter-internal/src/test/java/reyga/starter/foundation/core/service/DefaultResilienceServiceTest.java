package reyga.starter.foundation.core.service;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import reyga.starter.foundation.common.logging.CommonLogger;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class DefaultResilienceServiceTest {

    private DefaultResilienceService service;
    private CommonLogger logger;

    @BeforeEach
    void setUp() {
        service = new DefaultResilienceService();
        logger = mock(CommonLogger.class);
        service.logger = logger;
    }

    @Test
    void should_ReturnSuppliedValueWithoutFallback_When_RateLimiterPermitsRequest() {
        // given
        RateLimiterConfig config = rateLimiterConfig();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        AtomicInteger suppliedCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();

        // when
        String result = service.useRateLimiter(
                config,
                registry,
                "supplier-success",
                () -> {
                    suppliedCalls.incrementAndGet();
                    return "success";
                },
                () -> {
                    fallbackCalls.incrementAndGet();
                    return "fallback";
                }
        );

        // then
        assertEquals("success", result);
        assertEquals(1, suppliedCalls.get());
        assertEquals(0, fallbackCalls.get());
        verifyNoInteractions(logger);
    }

    @Test
    void should_ReturnFallbackAndLogWarning_When_RateLimiterRejectsSupplier() {
        // given
        RateLimiterConfig config = rateLimiterConfig();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        RateLimiter rateLimiter = registry.rateLimiter("supplier-rejected", config);
        assertTrue(rateLimiter.acquirePermission());
        AtomicInteger suppliedCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();

        // when
        String result = service.useRateLimiter(
                config,
                registry,
                "supplier-rejected",
                () -> {
                    suppliedCalls.incrementAndGet();
                    return "success";
                },
                () -> {
                    fallbackCalls.incrementAndGet();
                    return "fallback";
                }
        );

        // then
        assertEquals("fallback", result);
        assertEquals(0, suppliedCalls.get());
        assertEquals(1, fallbackCalls.get());
        verify(logger).warn(
                org.mockito.ArgumentMatchers.eq("Request Not Permitted :"),
                org.mockito.ArgumentMatchers.anyString()
        );
        verifyNoMoreInteractions(logger);
    }

    @Test
    void should_UseCachedRateLimiterWithoutRegistryWrite_When_CacheContainsLimiter() {
        // given
        RateLimiterConfig config = rateLimiterConfig();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        RateLimiter cachedRateLimiter = RateLimiter.of("cached", config);
        Cache cache = mock(Cache.class);
        when(cache.get("cached", RateLimiter.class)).thenReturn(cachedRateLimiter);

        // when
        String result = service.useRateLimiterWithCache(
                config, registry, cache, "cached", () -> "success", () -> "fallback"
        );

        // then
        assertEquals("success", result);
        verify(cache).get("cached", RateLimiter.class);
        verifyNoMoreInteractions(cache);
        verifyNoInteractions(logger);
    }

    @Test
    void should_CreateCacheEntryAndReturnValue_When_CacheDoesNotContainLimiter() {
        // given
        RateLimiterConfig config = rateLimiterConfig();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        Cache cache = mock(Cache.class);
        when(cache.get("cache-miss", RateLimiter.class)).thenReturn(null);

        // when
        String result = service.useRateLimiterWithCache(
                config, registry, cache, "cache-miss", () -> "success", () -> "fallback"
        );

        // then
        assertEquals("success", result);
        verify(cache).get("cache-miss", RateLimiter.class);
        verify(cache).put(
                org.mockito.ArgumentMatchers.eq("cache-miss"),
                org.mockito.ArgumentMatchers.isA(RateLimiter.class)
        );
        verifyNoMoreInteractions(cache);
        verifyNoInteractions(logger);
    }

    @Test
    void should_UseRegistryAndContinue_When_CacheReadAndWriteFail() {
        // given
        RateLimiterConfig config = rateLimiterConfig();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        Cache cache = mock(Cache.class);
        when(cache.get("cache-failure", RateLimiter.class))
                .thenThrow(new IllegalStateException("read failed"));
        org.mockito.Mockito.doThrow(new IllegalStateException("write failed"))
                .when(cache).put(
                        org.mockito.ArgumentMatchers.eq("cache-failure"),
                        org.mockito.ArgumentMatchers.any(RateLimiter.class)
                );

        // when
        String result = service.useRateLimiterWithCache(
                config, registry, cache, "cache-failure", () -> "success", () -> "fallback"
        );

        // then
        assertEquals("success", result);
        verify(cache).get("cache-failure", RateLimiter.class);
        verify(cache).put(
                org.mockito.ArgumentMatchers.eq("cache-failure"),
                org.mockito.ArgumentMatchers.isA(RateLimiter.class)
        );
        verifyNoMoreInteractions(cache);
        verifyNoInteractions(logger);
    }

    @Test
    void should_ReturnSuppliedValueWithoutFallback_When_CircuitBreakerSupplierSucceeds() {
        // given
        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        AtomicInteger fallbackCalls = new AtomicInteger();

        // when
        String result = service.useCircuitBreaker(
                config, registry, "circuit-success", () -> "success",
                () -> {
                    fallbackCalls.incrementAndGet();
                    return "fallback";
                }
        );

        // then
        assertEquals("success", result);
        assertEquals(0, fallbackCalls.get());
        verifyNoInteractions(logger);
    }

    @Test
    void should_ReturnFallback_When_CircuitBreakerSupplierThrowsException() {
        // given
        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        AtomicInteger fallbackCalls = new AtomicInteger();

        // when
        String result = service.useCircuitBreaker(
                config,
                registry,
                "circuit-failure",
                () -> {
                    throw new IllegalStateException("failure");
                },
                () -> {
                    fallbackCalls.incrementAndGet();
                    return "fallback";
                }
        );

        // then
        assertEquals("fallback", result);
        assertEquals(1, fallbackCalls.get());
        verifyNoInteractions(logger);
    }

    @Test
    void should_ReturnValueOnFirstAttempt_When_RetrySupplierSucceeds() {
        // given
        RetryConfig config = retryConfig(3);
        RetryRegistry registry = RetryRegistry.of(config);
        AtomicInteger suppliedCalls = new AtomicInteger();

        // when
        String result = service.useRetry(config, registry, "retry-success", () -> {
            suppliedCalls.incrementAndGet();
            return "success";
        });

        // then
        assertEquals("success", result);
        assertEquals(1, suppliedCalls.get());
        verifyNoInteractions(logger);
    }

    @Test
    void should_PropagateLastExceptionAfterAllAttempts_When_RetrySupplierAlwaysFails() {
        // given
        RetryConfig config = retryConfig(3);
        RetryRegistry registry = RetryRegistry.of(config);
        AtomicInteger suppliedCalls = new AtomicInteger();

        // when
        IllegalStateException result = assertThrows(IllegalStateException.class, () ->
                service.useRetry(config, registry, "retry-failure", () -> {
                    suppliedCalls.incrementAndGet();
                    throw new IllegalStateException("failure");
                })
        );

        // then
        assertEquals("failure", result.getMessage());
        assertEquals(3, suppliedCalls.get());
        verifyNoInteractions(logger);
    }

    @Test
    void should_RunProcessWithoutFallback_When_RateLimiterPermitsRunnable() {
        // given
        RateLimiterConfig config = rateLimiterConfig();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        AtomicInteger processCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();

        // when
        service.useRateLimiter(
                config, registry, "runnable-success",
                (Runnable) processCalls::incrementAndGet,
                (Runnable) fallbackCalls::incrementAndGet
        );

        // then
        assertEquals(1, processCalls.get());
        assertEquals(0, fallbackCalls.get());
        verifyNoInteractions(logger);
    }

    @Test
    void should_RunFallbackAndLogWarning_When_RateLimiterRejectsRunnable() {
        // given
        RateLimiterConfig config = rateLimiterConfig();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        RateLimiter rateLimiter = registry.rateLimiter("runnable-rejected", config);
        assertTrue(rateLimiter.acquirePermission());
        AtomicInteger processCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();

        // when
        service.useRateLimiter(
                config, registry, "runnable-rejected",
                (Runnable) processCalls::incrementAndGet,
                (Runnable) fallbackCalls::incrementAndGet
        );

        // then
        assertEquals(0, processCalls.get());
        assertEquals(1, fallbackCalls.get());
        verify(logger).warn(
                org.mockito.ArgumentMatchers.eq("Request Not Permitted :"),
                org.mockito.ArgumentMatchers.anyString()
        );
        verifyNoMoreInteractions(logger);
    }

    @Test
    void should_RunFallbackWithoutLogging_When_CachedRateLimiterRejectsRunnable() {
        // given
        RateLimiterConfig config = rateLimiterConfig();
        RateLimiterRegistry registry = RateLimiterRegistry.of(config);
        RateLimiter cachedRateLimiter = RateLimiter.of("cached-runnable", config);
        assertTrue(cachedRateLimiter.acquirePermission());
        Cache cache = mock(Cache.class);
        when(cache.get("cached-runnable", RateLimiter.class)).thenReturn(cachedRateLimiter);
        AtomicInteger processCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();

        // when
        service.useRateLimiterWithCache(
                config, registry, cache, "cached-runnable",
                (Runnable) processCalls::incrementAndGet,
                (Runnable) fallbackCalls::incrementAndGet
        );

        // then
        assertEquals(0, processCalls.get());
        assertEquals(1, fallbackCalls.get());
        verify(cache).get("cached-runnable", RateLimiter.class);
        verifyNoMoreInteractions(cache);
        verifyNoInteractions(logger);
    }

    @Test
    void should_RunProcessWithoutFallback_When_CircuitBreakerRunnableSucceeds() {
        // given
        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        AtomicInteger processCalls = new AtomicInteger();
        AtomicInteger fallbackCalls = new AtomicInteger();

        // when
        service.useCircuitBreaker(
                config, registry, "circuit-runnable-success",
                (Runnable) processCalls::incrementAndGet,
                (Runnable) fallbackCalls::incrementAndGet
        );

        // then
        assertEquals(1, processCalls.get());
        assertEquals(0, fallbackCalls.get());
        verifyNoInteractions(logger);
    }

    @Test
    void should_RunFallback_When_CircuitBreakerRunnableThrowsException() {
        // given
        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        AtomicInteger fallbackCalls = new AtomicInteger();

        // when
        service.useCircuitBreaker(
                config,
                registry,
                "circuit-runnable-failure",
                () -> {
                    throw new IllegalStateException("failure");
                },
                (Runnable) fallbackCalls::incrementAndGet
        );

        // then
        assertEquals(1, fallbackCalls.get());
        verifyNoInteractions(logger);
    }

    @Test
    void should_RunRunnableForAllAttemptsAndPropagateException_When_RetryAlwaysFails() {
        // given
        RetryConfig config = retryConfig(3);
        RetryRegistry registry = RetryRegistry.of(config);
        AtomicInteger processCalls = new AtomicInteger();
        IllegalStateException expected = new IllegalStateException("failure");

        // when
        IllegalStateException result = assertThrows(IllegalStateException.class, () ->
                service.useRetry(config, registry, "retry-runnable-failure", () -> {
                    processCalls.incrementAndGet();
                    throw expected;
                })
        );

        // then
        assertSame(expected, result);
        assertEquals(3, processCalls.get());
        verifyNoInteractions(logger);
    }

    private RateLimiterConfig rateLimiterConfig() {
        return RateLimiterConfig.custom()
                .limitForPeriod(1)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build();
    }

    private RetryConfig retryConfig(int maxAttempts) {
        return RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ZERO)
                .build();
    }
}

package reyga.starter.foundation.core.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.ResponseData;
import reyga.starter.foundation.core.service.foundation.ResilienceService;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ResilienceBaseControllerTest {

    private static final String KEY = "test-key";

    private ResilienceService resilienceService;
    private RateLimiterRegistry rateLimiterRegistry;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private RateLimiterConfig rateLimiterConfig;
    private CircuitBreakerConfig circuitBreakerConfig;
    private Cache cache;
    private TestResilienceController controller;

    @BeforeEach
    void setUp() {
        resilienceService = mock(ResilienceService.class);
        rateLimiterRegistry = mock(RateLimiterRegistry.class);
        circuitBreakerRegistry = mock(CircuitBreakerRegistry.class);
        rateLimiterConfig = mock(RateLimiterConfig.class);
        circuitBreakerConfig = mock(CircuitBreakerConfig.class);
        cache = mock(Cache.class);
        controller = new TestResilienceController(resilienceService, rateLimiterRegistry, circuitBreakerRegistry);
    }

    @Test
    void should_ReturnOkResponse_When_RateLimiterAllowsRequest() {
        // Given
        stubRateLimiter(false, true);

        // When
        ResponseEntity<String> result = controller.rateLimited("payload", rateLimiterConfig, KEY);

        // Then
        assertRawResponse(result, HttpStatus.OK, "payload");
        verifyRateLimiterInvocation(false);
    }

    @Test
    void should_ReturnTooManyRequests_When_RateLimiterRejectsRequest() {
        // Given
        stubRateLimiter(false, false);

        // When
        ResponseEntity<String> result = controller.rateLimited("payload", rateLimiterConfig, KEY);

        // Then
        assertRawResponse(result, HttpStatus.TOO_MANY_REQUESTS, "payload");
        verifyRateLimiterInvocation(false);
    }

    @Test
    void should_ReturnOkResponse_When_CachedRateLimiterAllowsRequest() {
        // Given
        stubRateLimiter(true, true);

        // When
        ResponseEntity<String> result = controller.cachedRateLimited("payload", rateLimiterConfig, KEY, cache);

        // Then
        assertRawResponse(result, HttpStatus.OK, "payload");
        verifyRateLimiterInvocation(true);
    }

    @Test
    void should_ReturnTooManyRequests_When_CachedRateLimiterRejectsRequest() {
        // Given
        stubRateLimiter(true, false);

        // When
        ResponseEntity<String> result = controller.cachedRateLimited("payload", rateLimiterConfig, KEY, cache);

        // Then
        assertRawResponse(result, HttpStatus.TOO_MANY_REQUESTS, "payload");
        verifyRateLimiterInvocation(true);
    }

    @Test
    void should_ReturnSuccessEnvelope_When_RateLimiterAllowsWrappedRequest() {
        // Given
        stubWrappedRateLimiter(false, true);

        // When
        ResponseEntity<ResponseData<String>> result = controller.wrappedRateLimited(
                "payload", "00", "success", rateLimiterConfig, KEY
        );

        // Then
        assertSuccessEnvelope(result);
        verifyRateLimiterInvocation(false);
    }

    @Test
    void should_ReturnFailureEnvelope_When_RateLimiterRejectsWrappedRequest() {
        // Given
        stubWrappedRateLimiter(false, false);

        // When
        ResponseEntity<ResponseData<String>> result = controller.wrappedRateLimited(
                "payload", "00", "success", rateLimiterConfig, KEY
        );

        // Then
        assertRateLimitFailureEnvelope(result);
        verifyRateLimiterInvocation(false);
    }

    @Test
    void should_ReturnSuccessEnvelope_When_CachedRateLimiterAllowsWrappedRequest() {
        // Given
        stubWrappedRateLimiter(true, true);

        // When
        ResponseEntity<ResponseData<String>> result = controller.cachedWrappedRateLimited(
                "payload", "00", "success", rateLimiterConfig, KEY, cache
        );

        // Then
        assertSuccessEnvelope(result);
        verifyRateLimiterInvocation(true);
    }

    @Test
    void should_ReturnFailureEnvelope_When_CachedRateLimiterRejectsWrappedRequest() {
        // Given
        stubWrappedRateLimiter(true, false);

        // When
        ResponseEntity<ResponseData<String>> result = controller.cachedWrappedRateLimited(
                "payload", "00", "success", rateLimiterConfig, KEY, cache
        );

        // Then
        assertRateLimitFailureEnvelope(result);
        verifyRateLimiterInvocation(true);
    }

    @Test
    void should_ReturnOkResponse_When_CircuitBreakerAllowsRequest() {
        // Given
        stubCircuitBreaker(true, false);

        // When
        ResponseEntity<String> result = controller.circuitBroken("payload", circuitBreakerConfig, KEY);

        // Then
        assertRawResponse(result, HttpStatus.OK, "payload");
        verifyCircuitBreakerInvocation();
    }

    @Test
    void should_ReturnInternalServerError_When_CircuitBreakerUsesFallback() {
        // Given
        stubCircuitBreaker(false, false);

        // When
        ResponseEntity<String> result = controller.circuitBroken("payload", circuitBreakerConfig, KEY);

        // Then
        assertRawResponse(result, HttpStatus.INTERNAL_SERVER_ERROR, "payload");
        verifyCircuitBreakerInvocation();
    }

    @Test
    void should_ReturnSuccessEnvelope_When_CircuitBreakerAllowsWrappedRequest() {
        // Given
        stubCircuitBreaker(true, true);

        // When
        ResponseEntity<ResponseData<String>> result = controller.wrappedCircuitBroken(
                "payload", "00", "success", circuitBreakerConfig, KEY
        );

        // Then
        assertSuccessEnvelope(result);
        verifyCircuitBreakerInvocation();
    }

    @Test
    void should_ReturnFailureEnvelope_When_CircuitBreakerUsesWrappedFallback() {
        // Given
        stubCircuitBreaker(false, true);

        // When
        ResponseEntity<ResponseData<String>> result = controller.wrappedCircuitBroken(
                "payload", "00", "success", circuitBreakerConfig, KEY
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(ServiceStatusResponseEnum.FAILED.getLabel(), result.getBody().getStatus());
        assertEquals(ServiceCodeEnum.GLOBAL_ERROR.getCode(), result.getBody().getCode());
        assertEquals(ServiceCodeEnum.GLOBAL_ERROR.getMessage(), result.getBody().getMessage());
        assertNull(result.getBody().getData());
        verifyCircuitBreakerInvocation();
    }

    private void stubRateLimiter(boolean withCache, boolean useSuccess) {
        if (withCache) {
            when(resilienceService.<ResponseEntity<String>>useRateLimiterWithCache(
                    same(rateLimiterConfig), same(rateLimiterRegistry), same(cache), eq(KEY),
                    anyStringResponseSupplier(), anyStringResponseSupplier()
            )).thenAnswer(invocation -> supplierResult(invocation, useSuccess ? 4 : 5));
        } else {
            when(resilienceService.<ResponseEntity<String>>useRateLimiter(
                    same(rateLimiterConfig), same(rateLimiterRegistry), eq(KEY),
                    anyStringResponseSupplier(), anyStringResponseSupplier()
            )).thenAnswer(invocation -> supplierResult(invocation, useSuccess ? 3 : 4));
        }
    }

    private void stubWrappedRateLimiter(boolean withCache, boolean useSuccess) {
        if (withCache) {
            when(resilienceService.<ResponseEntity<ResponseData<String>>>useRateLimiterWithCache(
                    same(rateLimiterConfig), same(rateLimiterRegistry), same(cache), eq(KEY),
                    anyWrappedResponseSupplier(), anyWrappedResponseSupplier()
            )).thenAnswer(invocation -> supplierResult(invocation, useSuccess ? 4 : 5));
        } else {
            when(resilienceService.<ResponseEntity<ResponseData<String>>>useRateLimiter(
                    same(rateLimiterConfig), same(rateLimiterRegistry), eq(KEY),
                    anyWrappedResponseSupplier(), anyWrappedResponseSupplier()
            )).thenAnswer(invocation -> supplierResult(invocation, useSuccess ? 3 : 4));
        }
    }

    private void stubCircuitBreaker(boolean useSuccess, boolean wrapped) {
        if (wrapped) {
            when(resilienceService.<ResponseEntity<ResponseData<String>>>useCircuitBreaker(
                    same(circuitBreakerConfig), same(circuitBreakerRegistry), eq(KEY),
                    anyWrappedResponseSupplier(), anyWrappedResponseSupplier()
            )).thenAnswer(invocation -> supplierResult(invocation, useSuccess ? 3 : 4));
        } else {
            when(resilienceService.<ResponseEntity<String>>useCircuitBreaker(
                    same(circuitBreakerConfig), same(circuitBreakerRegistry), eq(KEY),
                    anyStringResponseSupplier(), anyStringResponseSupplier()
            )).thenAnswer(invocation -> supplierResult(invocation, useSuccess ? 3 : 4));
        }
    }

    private Object supplierResult(org.mockito.invocation.InvocationOnMock invocation, int index) {
        return invocation.<Supplier<?>>getArgument(index).get();
    }

    private Supplier<ResponseEntity<String>> anyStringResponseSupplier() {
        return any();
    }

    private Supplier<ResponseEntity<ResponseData<String>>> anyWrappedResponseSupplier() {
        return any();
    }

    private void verifyRateLimiterInvocation(boolean withCache) {
        if (withCache) {
            verify(resilienceService).useRateLimiterWithCache(
                    same(rateLimiterConfig), same(rateLimiterRegistry), same(cache), eq(KEY),
                    anyStringResponseSupplier(), anyStringResponseSupplier()
            );
        } else {
            verify(resilienceService).useRateLimiter(
                    same(rateLimiterConfig), same(rateLimiterRegistry), eq(KEY),
                    anyStringResponseSupplier(), anyStringResponseSupplier()
            );
        }
        verifyNoMoreInteractions(resilienceService);
        verifyNoInteractions(rateLimiterRegistry, circuitBreakerRegistry, cache);
    }

    private void verifyCircuitBreakerInvocation() {
        verify(resilienceService).useCircuitBreaker(
                same(circuitBreakerConfig), same(circuitBreakerRegistry), eq(KEY),
                anyStringResponseSupplier(), anyStringResponseSupplier()
        );
        verifyNoMoreInteractions(resilienceService);
        verifyNoInteractions(rateLimiterRegistry, circuitBreakerRegistry, cache);
    }

    private static void assertRawResponse(ResponseEntity<String> response, HttpStatus status, String body) {
        assertEquals(status, response.getStatusCode());
        assertEquals(body, response.getBody());
        assertTrue(response.getHeaders().isEmpty());
    }

    private static void assertSuccessEnvelope(ResponseEntity<ResponseData<String>> response) {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ServiceStatusResponseEnum.SUCCESS.getLabel(), response.getBody().getStatus());
        assertEquals("00", response.getBody().getCode());
        assertEquals("success", response.getBody().getMessage());
        assertEquals("payload", response.getBody().getData());
    }

    private static void assertRateLimitFailureEnvelope(ResponseEntity<ResponseData<String>> response) {
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ServiceStatusResponseEnum.FAILED.getLabel(), response.getBody().getStatus());
        assertEquals(ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getCode(), response.getBody().getCode());
        assertEquals(ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getMessage(), response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    private static final class TestResilienceController extends ResilienceBaseController {
        private TestResilienceController(ResilienceService resilienceService, RateLimiterRegistry rateLimiterRegistry,
                                         CircuitBreakerRegistry circuitBreakerRegistry) {
            super(resilienceService, rateLimiterRegistry, circuitBreakerRegistry);
        }

        <T> ResponseEntity<T> rateLimited(T data, RateLimiterConfig config, String key) {
            return createResponseWithRateLimiter(data, config, key);
        }

        <T> ResponseEntity<T> cachedRateLimited(T data, RateLimiterConfig config, String key, Cache cache) {
            return createResponseWithRateLimiter(data, config, key, cache);
        }

        <T> ResponseEntity<ResponseData<T>> wrappedRateLimited(T data, String code, String message,
                                                               RateLimiterConfig config, String key) {
            return createResponseWithRateLimiter(data, code, message, config, key);
        }

        <T> ResponseEntity<ResponseData<T>> cachedWrappedRateLimited(T data, String code, String message,
                                                                     RateLimiterConfig config, String key, Cache cache) {
            return createResponseWithRateLimiter(data, code, message, config, key, cache);
        }

        <T> ResponseEntity<T> circuitBroken(T data, CircuitBreakerConfig config, String name) {
            return createResponseWithCircuitBreaker(data, config, name);
        }

        <T> ResponseEntity<ResponseData<T>> wrappedCircuitBroken(T data, String code, String message,
                                                                 CircuitBreakerConfig config, String name) {
            return createResponseWithCircuitBreaker(data, code, message, config, name);
        }
    }
}

package reyga.starter.foundation.core.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.ResponseData;
import reyga.starter.foundation.core.service.foundation.ResilienceService;

/**
 * Base controller providing resilience patterns such as Rate Limiting and Circuit Breaking.
 * Extends {@link BaseController} to inherit standard response building capabilities.
 */
@RequiredArgsConstructor
public abstract class ResilienceBaseController extends BaseController {

    private final ResilienceService resilienceService;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * Creates a response wrapped with a Rate Limiter.
     *
     * @param data the data to return
     * @param rlConfig the Rate Limiter configuration
     * @param rlKey the unique key for the Rate Limiter instance
     * @return ResponseEntity containing the data or a TOO_MANY_REQUESTS status
     */
    protected <T> ResponseEntity<T> createResponseWithRateLimiter(
            T data, RateLimiterConfig rlConfig, String rlKey
    ) {
        return resilienceService.useRateLimiter(rlConfig, rateLimiterRegistry, rlKey,
                () -> new ResponseEntity<>(data, HttpStatus.OK), ()-> new ResponseEntity<>(data, HttpStatus.TOO_MANY_REQUESTS)
        );
    }

    /**
     * Creates a response wrapped with a Rate Limiter and utilizes caching.
     *
     * @param data the data to return
     * @param rlConfig the Rate Limiter configuration
     * @param rlKey the unique key for the Rate Limiter instance
     * @param cache the cache instance to use
     * @return ResponseEntity containing the data or a TOO_MANY_REQUESTS status
     */
    protected <T> ResponseEntity<T> createResponseWithRateLimiter(
            T data, RateLimiterConfig rlConfig, String rlKey, Cache cache
    ) {
        return resilienceService.useRateLimiterWithCache(rlConfig, rateLimiterRegistry, cache, rlKey,
                () -> new ResponseEntity<>(data, HttpStatus.OK), () -> new ResponseEntity<>(data, HttpStatus.TOO_MANY_REQUESTS)
        );
    }

    /**
     * Creates a standardized ResponseData wrapped with a Rate Limiter.
     *
     * @param data the data to return
     * @param code the service-specific success code
     * @param message the success message
     * @param rlConfig the Rate Limiter configuration
     * @param rlKey the unique key for the Rate Limiter instance
     * @return ResponseEntity containing ResponseData or a standardized error response
     */
    protected <T> ResponseEntity<ResponseData<T>> createResponseWithRateLimiter(
            T data, String code, String message, RateLimiterConfig rlConfig, String rlKey
    ) {
        return resilienceService.useRateLimiter(rlConfig, rateLimiterRegistry, rlKey,
                () -> new ResponseEntity<>(
                        buildResponseData(data, ServiceStatusResponseEnum.SUCCESS.getLabel(), code, message), HttpStatus.OK
                ),
                ()-> new ResponseEntity<>(
                        buildResponseData(null, ServiceStatusResponseEnum.FAILED.getLabel(), ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getCode(),
                                ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getMessage()), HttpStatus.TOO_MANY_REQUESTS
                )
        );
    }

    /**
     * Creates a standardized ResponseData wrapped with a Rate Limiter and utilizes caching.
     *
     * @param data the data to return
     * @param code the service-specific success code
     * @param message the success message
     * @param rlConfig the Rate Limiter configuration
     * @param rlKey the unique key for the Rate Limiter instance
     * @param cache the cache instance to use
     * @return ResponseEntity containing ResponseData or a standardized error response
     */
    protected <T> ResponseEntity<ResponseData<T>> createResponseWithRateLimiter(
            T data, String code, String message, RateLimiterConfig rlConfig, String rlKey, Cache cache
    ) {
        return resilienceService.useRateLimiterWithCache(rlConfig, rateLimiterRegistry, cache, rlKey,
                () -> new ResponseEntity<>(
                        buildResponseData(data, ServiceStatusResponseEnum.SUCCESS.getLabel(), code, message), HttpStatus.OK
                ), () -> new ResponseEntity<>(
                        buildResponseData(
                                null, ServiceStatusResponseEnum.FAILED.getLabel(), ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getCode(),
                                ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getMessage()
                        ), HttpStatus.TOO_MANY_REQUESTS
                )
        );
    }

    /**
     * Creates a response wrapped with a Circuit Breaker.
     *
     * @param data the data to return
     * @param cbConfig the Circuit Breaker configuration
     * @param cbName the name of the Circuit Breaker instance
     * @return ResponseEntity containing the data or an INTERNAL_SERVER_ERROR status
     */
    protected <T> ResponseEntity<T> createResponseWithCircuitBreaker(
            T data, CircuitBreakerConfig cbConfig, String cbName
    ) {
        return resilienceService.useCircuitBreaker(cbConfig, circuitBreakerRegistry, cbName,
                () -> new ResponseEntity<>(data, HttpStatus.OK), ()-> new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    /**
     * Creates a standardized ResponseData wrapped with a Circuit Breaker.
     *
     * @param data the data to return
     * @param code the service-specific success code
     * @param message the success message
     * @param cbConfig the Circuit Breaker configuration
     * @param cbName the name of the Circuit Breaker instance
     * @return ResponseEntity containing ResponseData or a standardized error response
     */
    protected <T> ResponseEntity<ResponseData<T>> createResponseWithCircuitBreaker(
            T data, String code, String message, CircuitBreakerConfig cbConfig, String cbName
    ) {
        return resilienceService.useCircuitBreaker(cbConfig, circuitBreakerRegistry, cbName,
                () -> new ResponseEntity<>(
                        buildResponseData(data, ServiceStatusResponseEnum.SUCCESS.getLabel(), code, message), HttpStatus.OK
                ),
                ()-> new ResponseEntity<>(
                        buildResponseData(null, ServiceStatusResponseEnum.FAILED.getLabel(), ServiceCodeEnum.GLOBAL_ERROR.getCode(),
                                ServiceCodeEnum.GLOBAL_ERROR.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR
                )
        );
    }

}

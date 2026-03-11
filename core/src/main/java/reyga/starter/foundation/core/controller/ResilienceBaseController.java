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
import reyga.starter.foundation.core.dto.response.ResponseData;
import reyga.starter.foundation.core.service.ResilienceService;

@RequiredArgsConstructor
public abstract class ResilienceBaseController extends BaseController {

    private final ResilienceService resilienceService;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    protected <T> ResponseEntity<T> createResponseWithRateLimiter(
            T data, RateLimiterConfig rlConfig, String rlKey
    ) {
        return resilienceService.useRateLimiter(rlConfig, rateLimiterRegistry, rlKey,
                () -> new ResponseEntity<>(data, HttpStatus.OK), ()-> new ResponseEntity<>(data, HttpStatus.TOO_MANY_REQUESTS)
        );
    }

    protected <T> ResponseEntity<T> createResponseWithRateLimiter(
            T data, RateLimiterConfig rlConfig, String rlKey, Cache cache
    ) {
        return resilienceService.useRateLimiterWithCache(rlConfig, rateLimiterRegistry, cache, rlKey,
                () -> new ResponseEntity<>(data, HttpStatus.OK), () -> new ResponseEntity<>(data, HttpStatus.TOO_MANY_REQUESTS)
        );
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponseWithRateLimiter(
            T data, String code, String message, RateLimiterConfig rlConfig, String rlKey
    ) {
        return resilienceService.useRateLimiter(rlConfig, rateLimiterRegistry, rlKey,
                () -> new ResponseEntity<>(
                        buildResponseData(data, ServiceStatusResponseEnum.SUCCESS.getValue(), code, message), HttpStatus.OK
                ),
                ()-> new ResponseEntity<>(
                        buildResponseData(null, ServiceStatusResponseEnum.FAILED.getValue(), ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getCode(),
                                ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getMessage()), HttpStatus.TOO_MANY_REQUESTS
                )
        );
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponseWithRateLimiter(
            T data, String code, String message, RateLimiterConfig rlConfig, String rlKey, Cache cache
    ) {
        return resilienceService.useRateLimiterWithCache(rlConfig, rateLimiterRegistry, cache, rlKey,
                () -> new ResponseEntity<>(
                        buildResponseData(data, ServiceStatusResponseEnum.SUCCESS.getValue(), code, message), HttpStatus.OK
                ), () -> new ResponseEntity<>(
                        buildResponseData(
                                null, ServiceStatusResponseEnum.FAILED.getValue(), ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getCode(),
                                ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getMessage()
                        ), HttpStatus.TOO_MANY_REQUESTS
                )
        );
    }

    protected <T> ResponseEntity<T> createResponseWithCircuitBreaker(
            T data, CircuitBreakerConfig cbConfig, String cbName
    ) {
        return resilienceService.useCircuitBreaker(cbConfig, circuitBreakerRegistry, cbName,
                () -> new ResponseEntity<>(data, HttpStatus.OK), ()-> new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponseWithCircuitBreaker(
            T data, String code, String message, CircuitBreakerConfig cbConfig, String cbName
    ) {
        return resilienceService.useCircuitBreaker(cbConfig, circuitBreakerRegistry, cbName,
                () -> new ResponseEntity<>(
                        buildResponseData(data, ServiceStatusResponseEnum.SUCCESS.getValue(), code, message), HttpStatus.OK
                ),
                ()-> new ResponseEntity<>(
                        buildResponseData(null, ServiceStatusResponseEnum.FAILED.getValue(), ServiceCodeEnum.GLOBAL_ERROR.getCode(),
                                ServiceCodeEnum.GLOBAL_ERROR.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR
                )
        );
    }

}

package reyga.starter.foundation.core.controller;

import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.core.dto.response.ResponseData;
import reyga.starter.foundation.core.service.ResilienceService;

@Component
@RequiredArgsConstructor
public abstract class BaseResilienceController extends BaseController {

    private final ResilienceService resilienceService;
    private final Cache<String, RateLimiter> localCache;
    private final RateLimiterRegistry rateLimiterRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    protected <T> ResponseEntity<T> createResponseWithRateLimiter(
            T data, RateLimiterConfig rlConfig, String rlKey
    ) {
        setMDCResponse(data);
        return resilienceService.useRateLimiter(rlConfig, rateLimiterRegistry, localCache, rlKey,
                () -> new ResponseEntity<>(data, HttpStatus.OK),
                ()-> new ResponseEntity<>(data, HttpStatus.TOO_MANY_REQUESTS)
        );
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponseWithRateLimiter(
            T data, String code, String message, RateLimiterConfig rlConfig, String rlKey
    ) {
        setMDCResponse(data);
        return resilienceService.useRateLimiter(rlConfig, rateLimiterRegistry, localCache, rlKey,
                () -> new ResponseEntity<>(
                        ResponseData.<T>builder()
                                .status(ServiceStatusResponseEnum.SUCCESS.getValue())
                                .code(code)
                                .message(message)
                                .data(data)
                                .build(), HttpStatus.OK
                ), ()-> new ResponseEntity<>(
                        ResponseData.<T>builder()
                                .status(ServiceStatusResponseEnum.FAILED.getValue())
                                .code(ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getCode())
                                .message(ServiceCodeEnum.RATE_LIMIT_EXCEEDED.getMessage())
                                .data(null)
                                .build(), HttpStatus.TOO_MANY_REQUESTS
                )
        );
    }

    protected <T> ResponseEntity<T> createResponseWithCircuitBreaker(
            T data, CircuitBreakerConfig cbConfig, String cbName
    ) {
        setMDCResponse(data);
        return resilienceService.useCircuitBreaker(cbConfig, circuitBreakerRegistry, cbName,
                () -> new ResponseEntity<>(data, HttpStatus.OK), ()-> new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponseWithCircuitBreaker(
            T data, String code, String message, CircuitBreakerConfig cbConfig, String cbName
    ) {
        setMDCResponse(data);
        return resilienceService.useCircuitBreaker(cbConfig, circuitBreakerRegistry, cbName,
                () -> new ResponseEntity<>(
                        ResponseData.<T>builder()
                                .status(ServiceStatusResponseEnum.SUCCESS.getValue())
                                .code(code)
                                .message(message)
                                .data(data)
                                .build(), HttpStatus.OK
                ), ()-> new ResponseEntity<>(
                        ResponseData.<T>builder()
                                .status(ServiceStatusResponseEnum.FAILED.getValue())
                                .code(ServiceCodeEnum.GLOBAL_ERROR.getCode())
                                .message(ServiceCodeEnum.GLOBAL_ERROR.getMessage())
                                .data(null)
                                .build(), HttpStatus.INTERNAL_SERVER_ERROR
                )
        );
    }
}

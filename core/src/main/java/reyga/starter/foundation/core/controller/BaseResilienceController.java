package reyga.starter.foundation.core.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reyga.starter.foundation.common.enumeration.ServiceCodeEnum;
import reyga.starter.foundation.common.enumeration.ServiceStatusResponseEnum;
import reyga.starter.foundation.common.model.dto.response.ResponseData;
import reyga.starter.foundation.core.service.ResilienceService;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public abstract class BaseResilienceController extends BaseController {
    private final ResilienceService resilienceService;
    private final ConcurrentHashMap<String, RateLimiter> concurrentHashMapRateLimit = new ConcurrentHashMap<>();

    protected <T> ResponseEntity<T> createResponseWithRateLimiter(
            T data, RateLimiterConfig rlConfig, String rlKey, String serviceName
    ) {
        return resilienceService.useRateLimiter(rlConfig, concurrentHashMapRateLimit, rlKey, serviceName,
                () -> new ResponseEntity<>(data, HttpStatus.OK), ()-> new ResponseEntity<>(data, HttpStatus.TOO_MANY_REQUESTS)
        );
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponseWithRateLimiter(
            T data, String code, String message, RateLimiterConfig rlConfig, String rlKey, String serviceName
    ) {
        return resilienceService.useRateLimiter(rlConfig, concurrentHashMapRateLimit, rlKey, serviceName,
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
            T data, CircuitBreakerConfig cbConfig, String serviceName
    ) {
        return resilienceService.useCircuitBreaker(cbConfig, serviceName,
                () -> new ResponseEntity<>(data, HttpStatus.OK), ()-> new ResponseEntity<>(data, HttpStatus.INTERNAL_SERVER_ERROR)
        );
    }

    protected <T> ResponseEntity<ResponseData<T>> createResponseWithCircuitBreaker(
            T data, String code, String message, CircuitBreakerConfig cbConfig, String serviceName
    ) {
        return resilienceService.useCircuitBreaker(cbConfig, serviceName,
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

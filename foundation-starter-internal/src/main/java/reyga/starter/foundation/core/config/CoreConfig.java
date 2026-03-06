package reyga.starter.foundation.core.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import reyga.starter.foundation.core.component.DefaultTransactionalExecutor;
import reyga.starter.foundation.core.component.TransactionalExecutor;
import reyga.starter.foundation.core.config.properties.ConfigProperties;
import reyga.starter.foundation.core.exception.handler.DefaultExceptionHandler;
import reyga.starter.foundation.core.service.DefaultResilienceService;
import reyga.starter.foundation.core.service.ResilienceService;
import reyga.starter.foundation.core.validation.ValidationProcessor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@Import({
        ConfigProperties.class,
        ConfigProperties.RateLimiter.class,
        ConfigProperties.CircuitBreaker.class,
        ConfigProperties.LocalCache.class,
        ConfigProperties.Exception.class,
        ConfigProperties.Validation.class
})
public class CoreConfig {

    @Bean
    @ConditionalOnProperty(name = "reyga.config.exception.enable-default", havingValue = "true")
    public DefaultExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.validation.enable-default", havingValue = "true")
    public ValidationProcessor validationProcessor() {
        return new ValidationProcessor();
    }

    @Bean
    @ConditionalOnMissingBean(ResilienceService.class)
    public ResilienceService resilienceService() {
        return new DefaultResilienceService();
    }

    @Bean
    @ConditionalOnMissingBean(TransactionalExecutor.class)
    public TransactionalExecutor transactionalExecutor(PlatformTransactionManager transactionManager) {
        return new DefaultTransactionalExecutor(transactionManager);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.rate-limiter.required", havingValue = "true")
    public RateLimiterConfig defaultRateLimiterConfig(ConfigProperties.RateLimiter rateLimiterProperty) {
        return RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(rateLimiterProperty.getRefreshPeriodSeconds()))
                .limitForPeriod(rateLimiterProperty.getMaxRequest())
                .timeoutDuration(Duration.ofMillis(rateLimiterProperty.getTimeoutMilis()))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.rate-limiter.required", havingValue = "true")
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig defaultConfig) {
        return RateLimiterRegistry.of(defaultConfig);
    }

    @Bean
    public Cache<String, RateLimiter> localCache(ConfigProperties.LocalCache cacheProperty) {
        return Caffeine.newBuilder()
                .expireAfterAccess(cacheProperty.getExpiresMinutes(), TimeUnit.MINUTES)
                .maximumSize(cacheProperty.getMaxSize())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.circuit-breaker.required", havingValue = "true")
    public CircuitBreakerConfig defaultCircuitBreakerConfig(ConfigProperties.CircuitBreaker circuitBreakerProperty) {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(circuitBreakerProperty.getFailureRateThreshold())
                .slowCallRateThreshold(circuitBreakerProperty.getSlowCallRateThreshold())
                .slowCallDurationThreshold(Duration.ofSeconds(circuitBreakerProperty.getSlowCallDurationThresholdSeconds()))
                .minimumNumberOfCalls(circuitBreakerProperty.getMinimumNumberOfCalls())
                .slidingWindowSize(circuitBreakerProperty.getSlidingWindowSize())
                .permittedNumberOfCallsInHalfOpenState(circuitBreakerProperty.getPermittedNumberOfCallsInHalfOpenState())
                .waitDurationInOpenState(Duration.ofSeconds(circuitBreakerProperty.getSlowCallDurationThresholdSeconds()))
                .automaticTransitionFromOpenToHalfOpenEnabled(circuitBreakerProperty.isAutomaticTransitionFromOpenToHalfOpenEnabled())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.circuit-breaker.required", havingValue = "true")
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        return CircuitBreakerRegistry.of(defaultConfig);
    }

}

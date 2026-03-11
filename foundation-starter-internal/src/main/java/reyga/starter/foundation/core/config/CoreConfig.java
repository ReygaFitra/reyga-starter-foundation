package reyga.starter.foundation.core.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import reyga.starter.foundation.core.aspect.AspectLogging;
import reyga.starter.foundation.core.aspect.BaseAspectProcessor;
import reyga.starter.foundation.core.aspect.DefaultAspectProcessor;
import reyga.starter.foundation.core.component.DefaultTransactionalExecutor;
import reyga.starter.foundation.core.component.TransactionalExecutor;
import reyga.starter.foundation.core.config.properties.ConfigProperties;
import reyga.starter.foundation.core.exception.handler.DefaultExceptionHandler;
import reyga.starter.foundation.core.service.DefaultResilienceService;
import reyga.starter.foundation.core.service.ResilienceService;
import reyga.starter.foundation.core.validation.ValidationProcessor;
import reyga.starter.foundation.logging.config.properties.LoggingProperties;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(ConfigProperties.class)
public class CoreConfig {

    private static final Logger log = LoggerFactory.getLogger(CoreConfig.class);

    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.exception-handler", havingValue = "true")
    public DefaultExceptionHandler defaultExceptionHandler() {
        return new DefaultExceptionHandler();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.validation-handler", havingValue = "true")
    public ValidationProcessor validationProcessor() {
        return new ValidationProcessor();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.utilities", havingValue = "true")
    public ResilienceService resilienceService() {
        return new DefaultResilienceService();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.default-bean.utilities", havingValue = "true")
    public TransactionalExecutor transactionalExecutor(PlatformTransactionManager transactionManager) {
        return new DefaultTransactionalExecutor(transactionManager);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.custom.logging.enable-aspect-logging", havingValue = "true")
    public AspectLogging loggingAspect(LoggingProperties loggingProperties, BaseAspectProcessor baseAspectProcessor) {
        return new AspectLogging(loggingProperties.enableAspectLogging(), baseAspectProcessor);
    }

    @Bean
    @ConditionalOnMissingBean(BaseAspectProcessor.class)
    public BaseAspectProcessor defaultAspectProcessor() {
        return new DefaultAspectProcessor();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.rate-limiter.required", havingValue = "true")
    public RateLimiterConfig defaultRateLimiterConfig(ConfigProperties properties) {
        ConfigProperties.RateLimiter rateLimiterProperty = properties.rateLimiter();
        return RateLimiterConfig.custom()
                .limitRefreshPeriod(Duration.ofSeconds(rateLimiterProperty.refreshPeriodSeconds()))
                .limitForPeriod(rateLimiterProperty.maxRequest())
                .timeoutDuration(Duration.ofMillis(rateLimiterProperty.timeoutMilis()))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.rate-limiter.required", havingValue = "true")
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig defaultConfig) {
        return RateLimiterRegistry.of(defaultConfig);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.circuit-breaker.required", havingValue = "true")
    public CircuitBreakerConfig defaultCircuitBreakerConfig(ConfigProperties properties) {
        ConfigProperties.CircuitBreaker circuitBreakerProperty = properties.circuitBreaker();
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(circuitBreakerProperty.failureRateThreshold())
                .slowCallRateThreshold(circuitBreakerProperty.slowCallRateThreshold())
                .slowCallDurationThreshold(Duration.ofSeconds(circuitBreakerProperty.slowCallDurationThresholdSeconds()))
                .minimumNumberOfCalls(circuitBreakerProperty.minimumNumberOfCalls())
                .slidingWindowSize(circuitBreakerProperty.slidingWindowSize())
                .permittedNumberOfCallsInHalfOpenState(circuitBreakerProperty.permittedNumberOfCallsInHalfOpenState())
                .waitDurationInOpenState(Duration.ofSeconds(circuitBreakerProperty.slowCallDurationThresholdSeconds()))
                .automaticTransitionFromOpenToHalfOpenEnabled(circuitBreakerProperty.automaticTransitionFromOpenToHalfOpenEnabled())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.circuit-breaker.required", havingValue = "true")
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        return CircuitBreakerRegistry.of(defaultConfig);
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.retry.required", havingValue = "true")
    public RetryConfig defaultRetryConfig(ConfigProperties properties) {
        return RetryConfig.custom()
                .maxAttempts(properties.retry().maxAttempts())
                .waitDuration(Duration.ofMillis(properties.retry().waitDurationMillis()))
                .failAfterMaxAttempts(properties.retry().failAfterMaxAttempts())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "reyga.config.retry.required", havingValue = "true")
    public RetryRegistry retryRegistry(RetryConfig retryConfig) {
        RetryRegistry registry = RetryRegistry.of(retryConfig);
        registry.getEventPublisher()
                .onEntryAdded(event -> {
                    Retry addedRetry = event.getAddedEntry();
                    addedRetry.getEventPublisher().onRetry(retryEvent ->
                            log.warn(
                                    "-> Retry triggered [{}] attempt#{} because: {}",
                                    retryEvent.getName(),
                                    retryEvent.getNumberOfRetryAttempts(),
                                    retryEvent.getLastThrowable() != null
                                            ? retryEvent.getLastThrowable().getMessage()
                                            : "unknown error"
                            )
                    );
                });
        return registry;
    }

}

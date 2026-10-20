package reyga.starter.foundation.core.config;

import java.time.Duration;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import reyga.starter.foundation.core.config.properties.ConfigProperties;

/**
 * Configures the Resilience feature using existing bean names and properties.
 * Explicit legacy CoreConfig imports retain priority to avoid duplicate bean definitions.
 */
@AutoConfiguration
@ConditionalOnMissingBean(CoreConfig.class)
@EnableConfigurationProperties(ConfigProperties.class)
public class ResilienceAutoConfiguration {

    /** Creates this feature configuration. */
    public ResilienceAutoConfiguration() {
    }

    private static final Logger log = LoggerFactory.getLogger(CoreConfig.class);

    /**
     * Creates the existing defaultRateLimiterConfig bean with unchanged activation and defaults.
     * @param properties bound foundation properties supplying the resilience settings
     * @return the configured bean using the legacy factory behavior
     */
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

    /**
     * Creates the existing rateLimiterRegistry bean with unchanged activation and defaults.
     * @param defaultConfig configuration used for new instances in the registry
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.rate-limiter.required", havingValue = "true")
    public RateLimiterRegistry rateLimiterRegistry(RateLimiterConfig defaultConfig) {
        return RateLimiterRegistry.of(defaultConfig);
    }

    /**
     * Creates the existing defaultCircuitBreakerConfig bean with unchanged activation and defaults.
     * @param properties bound foundation properties supplying the resilience settings
     * @return the configured bean using the legacy factory behavior
     */
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

    /**
     * Creates the existing circuitBreakerRegistry bean with unchanged activation and defaults.
     * @param defaultConfig configuration used for new instances in the registry
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.circuit-breaker.required", havingValue = "true")
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig defaultConfig) {
        return CircuitBreakerRegistry.of(defaultConfig);
    }

    /**
     * Creates the existing defaultRetryConfig bean with unchanged activation and defaults.
     * @param properties bound foundation properties supplying the resilience settings
     * @return the configured bean using the legacy factory behavior
     */
    @Bean
    @ConditionalOnProperty(name = "reyga.config.retry.required", havingValue = "true")
    public RetryConfig defaultRetryConfig(ConfigProperties properties) {
        return RetryConfig.custom()
                .maxAttempts(properties.retry().maxAttempts())
                .waitDuration(Duration.ofMillis(properties.retry().waitDurationMillis()))
                .failAfterMaxAttempts(properties.retry().failAfterMaxAttempts())
                .build();
    }

    /**
     * Creates a retry registry and registers the existing warning listener on newly added retries.
     * @param retryConfig configuration used for new retry instances in the registry
     * @return the configured bean using the legacy factory behavior
     */
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
